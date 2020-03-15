package com.km.data.core.taskgroup;

import com.km.data.common.exception.DataETLException;
import com.km.data.common.util.Configuration;
import com.km.data.common.util.FrameworkErrorCode;
import com.km.data.core.AbstractContainer;
import com.km.data.core.enums.State;
import com.km.data.core.job.TaskRunner;
import com.km.data.core.statistics.communication.Communication;
import com.km.data.core.statistics.container.communicator.taskgroup.StandaloneTGContainerCommunicator;
import com.km.data.core.util.container.CoreConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskGroupContainer extends AbstractContainer {
    private static final Logger LOG = LoggerFactory
            .getLogger(TaskGroupContainer.class);

    /**
     * 当前taskGroup所属jobId
     */
    private long jobId;

    /**
     * 当前taskGroupId
     */
    private int taskGroupId;

    /**
     * task收集器使用的类
     */
    private String taskCollectorClass;

    private ExecutorService taskExecutorService;

    private TaskMonitor taskMonitor = TaskMonitor.getInstance();

    public TaskGroupContainer(Configuration configuration) {
        //构造方法仅保存了taskgroup全局配置文件
        super(configuration);

        initCommunicator(configuration);

        this.jobId = this.configuration.getLong(
                CoreConstant.DATAX_CORE_CONTAINER_JOB_ID);
        this.taskGroupId = this.configuration.getInt(
                CoreConstant.DATAX_CORE_CONTAINER_TASKGROUP_ID);

        this.taskCollectorClass = this.configuration.getString(
                CoreConstant.DATAX_CORE_STATISTICS_COLLECTOR_PLUGIN_TASKCLASS);
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

            int taskCountInThisTaskGroup = taskConfigs.size();

            this.containerCommunicator.registerCommunication(taskConfigs);

            startAllTask(this.configuration.getListConfiguration(CoreConstant.DATAX_JOB_CONTENT));


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

    private void startAllTask(List<Configuration> configurations) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(configurations.size());
        this.taskExecutorService = Executors
                .newFixedThreadPool(configurations.size());

        for (Configuration conf : configurations) {
            TaskRunner runner = new TaskRunner(conf, countDownLatch);
            taskExecutorService.execute(runner);
        }

        countDownLatch.await();
        this.taskExecutorService.shutdown();
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
        List<Configuration> remainTasks = new LinkedList<Configuration>();
        for (Configuration taskConfig : configurations) {
            remainTasks.add(taskConfig);
        }
        return remainTasks;
    }
}
