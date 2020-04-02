package com.km.data.core.taskgroup;

import com.km.data.common.exception.CommonErrorCode;
import com.km.data.common.exception.DataETLException;
import com.km.data.common.util.Configuration;
import com.km.data.common.util.FrameworkErrorCode;
import com.km.data.core.AbstractContainer;
import com.km.data.core.enums.State;
import com.km.data.core.statistics.communication.Communication;
import com.km.data.core.statistics.communication.CommunicationTool;
import com.km.data.core.statistics.communication.LocalTGCommunicationManager;
import com.km.data.core.statistics.container.communicator.AbstractContainerCommunicator;
import com.km.data.core.statistics.container.communicator.taskgroup.StandaloneTGContainerCommunicator;
import com.km.data.core.transport.channel.Channel;
import com.km.data.core.transport.channel.memory.MemoryChannel;
import com.km.data.core.util.container.CoreConstant;
import com.km.data.etl.ETL;
import com.km.data.reader.Reader;
import com.km.data.writer.Writer;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;


public class TaskGroupContainer extends AbstractContainer {
    private static final Logger LOG = LoggerFactory
            .getLogger(TaskGroupContainer.class);
    private LocalTGCommunicationManager tgCommunicationManager;
    /**
     * 当前taskGroup所属jobId
     */
    private long jobId;

    /**
     * 当前taskGroupId
     */
    private int taskGroupId;

    private TaskMonitor taskMonitor = TaskMonitor.getInstance();

    public TaskGroupContainer(Configuration configuration,LocalTGCommunicationManager tgCommunicationManager) {
        //构造方法仅保存了taskgroup全局配置文件
        super(configuration);

        initCommunicator(configuration);

        this.jobId = this.configuration.getLong(
                CoreConstant.DATAX_CORE_CONTAINER_JOB_ID);
        this.taskGroupId = this.configuration.getInt(
                CoreConstant.DATAX_CORE_CONTAINER_TASKGROUP_ID);
        this.tgCommunicationManager = tgCommunicationManager;
        this.getContainerCommunicator().getCollector().setTGCommunicationManager(tgCommunicationManager);
        this.getContainerCommunicator().getReporter().setTGCommunicationManager(tgCommunicationManager);
    }

    private void initCommunicator(Configuration configuration) {
        super.setContainerCommunicator(new StandaloneTGContainerCommunicator(configuration));
    }

    public long getJobId() {
        return jobId;
    }

    public int getTaskGroupId() {
        return taskGroupId;
    }

    @Override
    public void start() {
        try {
            // 获取channel数目
            int channelNumber = this.configuration.getInt(
                    CoreConstant.DATAX_CORE_CONTAINER_TASKGROUP_CHANNEL);

            List<Configuration> taskConfigs = this.configuration
                    .getListConfiguration(CoreConstant.DATAX_JOB_CONTENT);
            //任务失败最大重试次数
            int taskMaxRetryTimes = this.configuration.getInt(
                    CoreConstant.DATAX_CORE_CONTAINER_TASK_FAILOVER_MAXRETRYTIMES, 1);
            //任务失败重启时间间隔
            long taskRetryIntervalInMsec = this.configuration.getLong(
                    CoreConstant.DATAX_CORE_CONTAINER_TASK_FAILOVER_RETRYINTERVALINMSEC, 10000);
            //任务出错重试最长等待时间
            long taskMaxWaitInMsec = this.configuration.getLong(CoreConstant.DATAX_CORE_CONTAINER_TASK_FAILOVER_MAXWAITINMSEC, 60000);

            int taskCountInThisTaskGroup = taskConfigs.size();
            this.containerCommunicator.registerCommunication(taskConfigs);

            Map<Integer, Configuration> taskConfigMap = buildTaskConfigMap(taskConfigs); //taskId与task配置
            List<Configuration> taskQueue = buildRemainTasks(taskConfigs); //待运行task列表
            Map<Integer, TaskExecutor> taskFailedExecutorMap = new HashMap<>(); //taskId与上次失败实例
            List<TaskExecutor> runTasks = new ArrayList<>(channelNumber); //正在运行task

            Communication lastTaskGroupContainerCommunication = new Communication();

            while (true) {
                //1.判断task状态
                boolean failedOrKilled = false;
                Map<Integer, Communication> communicationMap = containerCommunicator.getCommunicationMap();
                for (Map.Entry<Integer, Communication> entry : communicationMap.entrySet()) {
                    Integer taskId = entry.getKey();
                    Communication taskCommunication = entry.getValue();
                    if (!taskCommunication.isFinished()) {
                        continue;
                    }
                    TaskExecutor taskExecutor = removeTask(runTasks, taskId);

                    taskMonitor.removeTask(taskId);
                    if (taskCommunication.getState() == State.FAILED) {
                        if (taskExecutor.getAttemptCount() < taskMaxRetryTimes) {
                            taskExecutor.shutdown(); //关闭老的executor
                            containerCommunicator.resetCommunication(taskId); //将task的状态重置
                            Configuration taskConfig = taskConfigMap.get(taskId);
                            taskQueue.add(taskConfig); //重新加入任务列表
                        } else {
                            failedOrKilled = true;
                            break;
                        }
                    } else if (taskCommunication.getState() == State.KILLED) {
                        failedOrKilled = true;
                        break;
                    } else if (taskCommunication.getState() == State.SUCCEEDED) {
                        taskConfigMap.remove(taskId);
                    }
                }
                // 2.发现该taskGroup下taskExecutor的总状态失败则汇报错误
                if (failedOrKilled) {
                    lastTaskGroupContainerCommunication = reportTaskGroupCommunication(
                            lastTaskGroupContainerCommunication, taskCountInThisTaskGroup);
                    throw DataETLException.asDataETLException(
                            FrameworkErrorCode.PLUGIN_RUNTIME_ERROR, lastTaskGroupContainerCommunication.getThrowable());
                }

                //3.有任务未执行，且正在运行的任务数小于最大通道限制
                Iterator<Configuration> iterator = taskQueue.iterator();
                while (iterator.hasNext() && runTasks.size() < channelNumber) {
                    Configuration taskConfig = iterator.next();
                    Integer taskId = taskConfig.getInt(CoreConstant.TASK_ID);
                    int attemptCount = 1;
                    TaskExecutor lastExecutor = taskFailedExecutorMap.get(taskId);
                    if (lastExecutor != null) {
                        attemptCount = lastExecutor.getAttemptCount() + 1;
                        long now = System.currentTimeMillis();
                        long failedTime = lastExecutor.getTimeStamp();
                        if (now - failedTime < taskRetryIntervalInMsec) {  //未到等待时间，继续留在队列
                            continue;
                        }
                        if (!lastExecutor.isShutdown()) { //上次失败的task仍未结束
                            if (now - failedTime > taskMaxWaitInMsec) {
                                markCommunicationFailed(taskId);
                                reportTaskGroupCommunication(lastTaskGroupContainerCommunication, taskCountInThisTaskGroup);
                                throw DataETLException.asDataETLException(CommonErrorCode.WAIT_TIME_EXCEED, "task failover等待超时");
                            } else {
                                lastExecutor.shutdown(); //再次尝试关闭
                                continue;
                            }
                        } else {
                            LOG.info("taskGroup[{}] taskId[{}] attemptCount[{}] has already shutdown",
                                    this.taskGroupId, taskId, lastExecutor.getAttemptCount());
                        }
                    }
                    Configuration taskConfigForRun = taskMaxRetryTimes > 1 ? taskConfig.clone() : taskConfig;
                    TaskExecutor taskExecutor = new TaskExecutor(taskConfigForRun, attemptCount);

                    taskExecutor.doStart();

                    iterator.remove();
                    runTasks.add(taskExecutor);

                    //上面，增加task到runTasks列表，因此在monitor里注册。
                    taskMonitor.registerTask(taskId, this.containerCommunicator.getCommunication(taskId));

                    taskFailedExecutorMap.remove(taskId);
                    LOG.info("taskGroup[{}] taskId[{}] attemptCount[{}] is started",
                            this.taskGroupId, taskId, attemptCount);
                }
                //4.任务列表为空，executor已结束, 搜集状态为success--->成功
                if (taskQueue.isEmpty() && isAllTaskDone(runTasks) && containerCommunicator.collectState() == State.SUCCEEDED) {
                    // 成功的情况下，也需要汇报一次。否则在任务结束非常快的情况下，采集的信息将会不准确
                    lastTaskGroupContainerCommunication = reportTaskGroupCommunication(
                            lastTaskGroupContainerCommunication, taskCountInThisTaskGroup);
                    LOG.info("taskGroup[{}] completed it's tasks.", this.taskGroupId);
                    break;
                }
            }

        } catch (Throwable e) {
            Communication nowTaskGroupContainerCommunication = this.containerCommunicator.collect();

            if (nowTaskGroupContainerCommunication.getThrowable() == null) {
                nowTaskGroupContainerCommunication.setThrowable(e);
            }
            nowTaskGroupContainerCommunication.setState(State.FAILED);
            this.containerCommunicator.report(nowTaskGroupContainerCommunication);

            throw DataETLException.asDataETLException(
                    FrameworkErrorCode.RUNTIME_ERROR, e);
        }
    }

    @Override
    public void post() {

    }

    @Override
    public void destory() {

    }

    private void markCommunicationFailed(Integer taskId) {
        Communication communication = containerCommunicator.getCommunication(taskId);
        communication.setState(State.FAILED);
    }

    private Communication reportTaskGroupCommunication(Communication lastTaskGroupContainerCommunication, int taskCount) {
        Communication nowTaskGroupContainerCommunication = this.containerCommunicator.collect();
        nowTaskGroupContainerCommunication.setTimestamp(System.currentTimeMillis());
        Communication reportCommunication = CommunicationTool.getReportCommunication(nowTaskGroupContainerCommunication,
                lastTaskGroupContainerCommunication, taskCount);
        this.containerCommunicator.report(reportCommunication);
        return reportCommunication;
    }

    private TaskExecutor removeTask(List<TaskExecutor> taskList, int taskId) {
        Iterator<TaskExecutor> iterator = taskList.iterator();
        while (iterator.hasNext()) {
            TaskExecutor taskExecutor = iterator.next();
            if (taskExecutor.getTaskId() == taskId) {
                iterator.remove();
                return taskExecutor;
            }
        }
        return null;
    }

    private Map<Integer, Configuration> buildTaskConfigMap(List<Configuration> configurations) {
        Map<Integer, Configuration> map = new HashMap<>();
        for (Configuration taskConfig : configurations) {
            int taskId = taskConfig.getInt(CoreConstant.TASK_ID);
            map.put(taskId, taskConfig);
        }
        return map;
    }

    private List<Configuration> buildRemainTasks(List<Configuration> configurations) {
        List<Configuration> remainTasks = new LinkedList<>();
        for (Configuration taskConfig : configurations) {
            remainTasks.add(taskConfig);
        }
        return remainTasks;
    }
    private boolean isAllTaskDone(List<TaskExecutor> taskList){
        for(TaskExecutor taskExecutor : taskList){
            if(!taskExecutor.isTaskFinished()){
                return false;
            }
        }
        return true;
    }

    public LocalTGCommunicationManager getTgCommunicationManager() {
        return tgCommunicationManager;
    }

    public void setTgCommunicationManager(LocalTGCommunicationManager tgCommunicationManager) {
        this.tgCommunicationManager = tgCommunicationManager;
    }

    /**
     * TaskExecutor是一个完整task的执行器
     * 其中包括1：1的reader和writer
     */
    class TaskExecutor {
        private Configuration taskConfig;

        private int taskId;

        private int attemptCount;

        private Channel channel;

        private Reader.Task reader;

        private Writer.Task writer;

        private Thread jobThread;


        /**
         * 该处的taskCommunication在多处用到：
         * 1. channel
         * 2. readerRunner和writerRunner
         * 3. reader和writer的taskPluginCollector
         */
        private Communication taskCommunication;

        public TaskExecutor(Configuration taskConf, int attemptCount) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
            // 获取该taskExecutor的配置
            this.taskConfig = taskConf;
            Validate.isTrue(null != this.taskConfig.getConfiguration(CoreConstant.JOB_READER)
                            && null != this.taskConfig.getConfiguration(CoreConstant.JOB_WRITER),
                    "[reader|writer]的插件参数不能为空!");

            // 得到taskId
            this.taskId = this.taskConfig.getInt(CoreConstant.TASK_ID);
            this.attemptCount = attemptCount;

            /**
             * 由taskId得到该taskExecutor的Communication
             * 要传给readerRunner和writerRunner，同时要传给channel作统计用
             */
            this.taskCommunication = containerCommunicator
                    .getCommunication(taskId);
            Validate.notNull(this.taskCommunication,
                    String.format("taskId[%d]的Communication没有注册过", taskId));
            this.channel = new MemoryChannel(taskConf);

            this.reader = createReader(taskConf);
            this.writer = createWriter(taskConf);

            jobThread = new Thread(() -> {
                try {
                    taskCommunication.setLongCounter(CommunicationTool.READ_SUCCEED_RECORDS, 0);
                    taskCommunication.setLongCounter(CommunicationTool.READ_SUCCEED_BYTES, 0);
                    taskCommunication.setLongCounter(CommunicationTool.WRITE_SUCCEED_RECORDS, 0);
                    taskCommunication.setLongCounter(CommunicationTool.WRITE_SUCCEED_BYTES, 0);
                    taskCommunication.setLongCounter(CommunicationTool.TOTAL_ETL_RECORDS, 0);
                    taskCommunication.setLongCounter(CommunicationTool.DONE_TASK_NUMBERS, 0);


                    reader.startRead(channel);
                    int recordSize = channel.size();
                    long recordBytes = channel.getTotalBytes();

                    taskCommunication.setLongCounter(CommunicationTool.READ_SUCCEED_RECORDS, recordSize);
                    taskCommunication.setLongCounter(CommunicationTool.READ_SUCCEED_BYTES, recordBytes);

                    if (taskConfig.getConfiguration("ETL") != null) {
                        ETL.process(channel, taskConfig.getConfiguration("ETL"));
                        taskCommunication.setLongCounter(CommunicationTool.TOTAL_ETL_RECORDS, recordSize);
                        recordSize = channel.size();
                        recordBytes = channel.getTotalBytes();
                    }
                    writer.startWrite(channel);
                    taskCommunication.setState(State.SUCCEEDED);
                    taskCommunication.setLongCounter(CommunicationTool.WRITE_SUCCEED_RECORDS, recordSize);
                    taskCommunication.setLongCounter(CommunicationTool.WRITE_SUCCEED_BYTES, recordBytes);
                    taskCommunication.setLongCounter(CommunicationTool.DONE_TASK_NUMBERS,taskCommunication.getLongCounter(CommunicationTool.DONE_TASK_NUMBERS)+1);
                }catch (Exception e){
                    taskCommunication.setThrowable(e);
                    taskCommunication.setState(State.FAILED);
                }
            });
        }


        private Reader.Task createReader(Configuration configuration) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
            String className = configuration.getString(CoreConstant.JOB_READER_PARAMETER + ".readerClassPath");
            return (Reader.Task) createTask(className, configuration.getConfiguration(CoreConstant.JOB_READER_PARAMETER));

        }

        private Writer.Task createWriter(Configuration configuration) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
            String className = configuration.getString(CoreConstant.JOB_WRITER_PARAMETER + ".writerClassPath");
            return (Writer.Task) createTask(className, configuration.getConfiguration(CoreConstant.JOB_WRITER_PARAMETER));
        }

        private Object createTask(String className, Configuration configuration) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
            Class clazz = Class.forName(className);
            Class innerClazz[] = clazz.getDeclaredClasses();
            Constructor constructor = null;
            for (Class cls : innerClazz) {
                if (cls.getName().contains("Task")) {
                    constructor = cls.getConstructor(Configuration.class);
                }
            }
            if (constructor == null) {
                throw DataETLException.asDataETLException
                        (CommonErrorCode.RUNTIME_ERROR, "您实现的reader或者writer插件缺少实现Task内部类");
            }
            return constructor.newInstance(configuration);
        }

        public void doStart() {
            jobThread.start();
        }

        // 检查任务是否结束
        private boolean isTaskFinished() {
            // 如果reader 或 writer没有完成工作，那么直接返回工作没有完成
            if (jobThread.isAlive()) {
                return false;
            }

            if (taskCommunication == null || !taskCommunication.isFinished()) {
                return false;
            }

            return true;
        }

        private int getTaskId() {
            return taskId;
        }

        private long getTimeStamp() {
            return taskCommunication.getTimestamp();
        }

        private int getAttemptCount() {
            return attemptCount;
        }

        private void shutdown() {
            if (jobThread.isAlive()) {
                jobThread.interrupt();
            }
        }

        private boolean isShutdown() {
            return !jobThread.isAlive();
        }

    }
}
