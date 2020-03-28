package com.km.data.core.job;


import com.alibaba.fastjson.JSONObject;
import com.km.data.common.exception.CommonErrorCode;
import com.km.data.common.exception.DataETLException;
import com.km.data.common.util.Configuration;
import com.km.data.core.AbstractContainer;
import com.km.data.core.job.scheduler.StandAloneScheduler;
import com.km.data.core.statistics.communication.Communication;
import com.km.data.core.statistics.container.communicator.AbstractContainerCommunicator;
import com.km.data.core.statistics.container.communicator.job.StandAloneJobContainerCommunicator;
import com.km.data.core.util.FrameworkErrorCode;
import com.km.data.core.util.JobAssignUtil;
import com.km.data.core.util.container.CoreConstant;
import com.km.data.reader.Reader;
import com.km.data.writer.Writer;
import com.km.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.km.data.core.statistics.communication.CommunicationTool;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * job实例运行在jobContainer容器中，它是所有任务的master，负责初始化、拆分、调度、运行、回收、监控和汇报
 * 但它并不做实际的数据同步操作
 */
public class JobContainer extends AbstractContainer {
    private static final Logger LOG = LoggerFactory
            .getLogger(JobContainer.class);

    private long jobId;

    private String readerPluginName;

    private String writerPluginName;

    /**
     * reader和writer jobContainer的实例
     */
    private Reader.Job jobReader;

    private Writer.Job jobWriter;

    private Integer needChannelNumber;

    private int taskNumber;


    public JobContainer(Configuration configuration) {
        super(configuration);
    }

    /**
     * jobContainer主要负责的工作全部在start()里面，包括init、split、scheduler
     */
    @Override
    public void start() {
        LOG.info("DataX jobContainer starts job.");
        try {
            LOG.debug("jobContainer starts to do init ...");
            this.init();
            LOG.info("jobContainer starts to do split ...");
            this.split();
            LOG.info("jobContainer starts to do schedule ...");
            this.schedule();
            LOG.debug("jobContainer starts to do post ...");
            while (true){
                Communication communication = this.getContainerCommunicator().collect();
                if(communication.getCounter().size()!=1){
                    System.out.println(6);
                }
                if(communication==null)
                    break;
            }

            this.post();
            this.destory();
            //this.getContainerCommunicator().getCollector().getTGCommunicationManager()
        } catch (Throwable e) {
            LOG.error("Exception when job run", e);

            if (e instanceof OutOfMemoryError) {
                System.gc();
            }
            throw DataETLException.asDataETLException(
                    FrameworkErrorCode.RUNTIME_ERROR, e);
        }
    }

    /**
     * reader和writer的初始化
     */
    public void init() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        this.readerPluginName = configuration.getString(CoreConstant.DATAX_JOB_CONTENT_READER_NAME);
        this.writerPluginName = configuration.getString(CoreConstant.DATAX_JOB_CONTENT_WRITER_NAME);

        String readerConfigPath = "src/main/resources/static/config/readerConfiguration.json";
        String writerConfigPath = "src/main/resources/static/config/writerConfiguration.json";
        Configuration readerConfig = Configuration.from(FileUtil.readFile(readerConfigPath));
        Configuration writerConfig = Configuration.from(FileUtil.readFile(writerConfigPath));

        List<JSONObject> readerPlugins = readerConfig.getList("reader", JSONObject.class);
        List<JSONObject> writerPlugins = writerConfig.getList("writer", JSONObject.class);

        String readerPluginClass = null;
        String writerPluginClass = null;

        for (JSONObject readerPluginConfig : readerPlugins) {
            if (readerPluginConfig.getString("name").equals(this.readerPluginName)) {
                readerPluginClass = readerPluginConfig.getString("classPath");
                break;
            }
        }
        if (readerPluginClass == null) {
            throw DataETLException.asDataETLException(CommonErrorCode.CONFIG_ERROR,
                    "配置的reader[ " + this.readerPluginName + "]不存在，请重新输入reader");
        }
        for (JSONObject writerPluginConfig : writerPlugins) {
            if (writerPluginConfig.getString("name").equals(this.writerPluginName)) {
                writerPluginClass = writerPluginConfig.getString("classPath");
                break;
            }
        }
        if (writerPluginClass == null) {
            throw DataETLException.asDataETLException(CommonErrorCode.CONFIG_ERROR,
                    "配置的writer[ " + this.writerPluginName + "]不存在，请重新输入writer");
        }
        configuration.set("test", "test");
        Configuration readerParameter = configuration.getConfiguration(CoreConstant.DATAX_JOB_CONTENT_READER_PARAMETER);
        Configuration writerParameter = configuration.getConfiguration(CoreConstant.DATAX_JOB_CONTENT_WRITER_PARAMETER);
        readerParameter.set("readerClassPath", readerPluginClass);
        writerParameter.set("writerClassPath", writerPluginClass);


        this.jobReader = (Reader.Job) createJob(readerPluginClass, readerParameter);
        this.jobWriter = (Writer.Job) createJob(writerPluginClass, writerParameter);
    }


    private Object createJob(String className, Configuration configuration) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Class clazz = Class.forName(className);
        Class innerClazz[] = clazz.getDeclaredClasses();
        Constructor constructor = null;
        for (Class cls : innerClazz) {
            if (cls.getName().contains("Job")) {
                constructor = cls.getConstructor(Configuration.class);
            }
        }
        if (constructor == null) {
            throw DataETLException.asDataETLException
                    (CommonErrorCode.RUNTIME_ERROR, "您实现的reader或者writer插件缺少实现Job内部类");
        }
        return constructor.newInstance(configuration);
    }

    private int split() {
        this.needChannelNumber = this.configuration.getInt(CoreConstant.DATAX_JOB_SETTING_SPEED_CHANNEL);
        //该方法通过读取配置文件，以及channel的数量，进行任务的切分，生成对应的configuration，
        // 在这里使用的是mysqlreader，因此切分后的configuration中，不同的地方仅仅只是sql的范围不同。
        //底层的split得自己去实现
        List<Configuration> readerTaskConfigs = this
                .doReaderSplit(this.needChannelNumber);
        this.taskNumber = readerTaskConfigs.size();
        List<Configuration> writerTaskConfigs = this
                .doWriterSplit(taskNumber);


        /**
         * 输入是reader和writer的parameter list，输出是content下面元素的list
         */
        List<Configuration> contentConfig = mergeReaderAndWriterTaskConfigs(
                readerTaskConfigs, writerTaskConfigs);

        if (configuration.get(CoreConstant.DATAX_JOB_CONTENT_ETL) != null)
            for (Configuration configuration : contentConfig) {
                configuration.set("ETL", this.configuration.getList(CoreConstant.DATAX_JOB_CONTENT_ETL, String.class));
            }

        this.configuration.set(CoreConstant.DATAX_JOB_CONTENT, contentConfig);
        return contentConfig.size();
    }

    public void schedule() {

        int channelsPerTaskGroup = this.configuration.getInt(
                CoreConstant.DATAX_CORE_CONTAINER_TASKGROUP_CHANNEL, 5);
        int taskNumber = this.configuration.getList(
                CoreConstant.DATAX_JOB_CONTENT).size();
        this.needChannelNumber = Math.min(this.needChannelNumber, taskNumber);
        List<Configuration> taskGroupConfigs = JobAssignUtil.assignFairly(this.configuration,
                this.needChannelNumber, channelsPerTaskGroup);
        try {
            StandAloneScheduler scheduler = initStandaloneScheduler(this.configuration);
            scheduler.schedule(taskGroupConfigs);
        } catch (Exception e) {
            throw DataETLException.asDataETLException(FrameworkErrorCode.RUNTIME_ERROR, e);
        }
    }


    public List<Configuration> doReaderSplit(int adviceNumber) {
        List<Configuration> readerSlicesConfigs = this.jobReader.split(adviceNumber);
        if (readerSlicesConfigs == null || readerSlicesConfigs.size() == 0) {
            throw DataETLException.asDataETLException(
                    FrameworkErrorCode.PLUGIN_SPLIT_ERROR,
                    "reader切分的task数目不能小于等于0");
        }
        return readerSlicesConfigs;
    }

    private List<Configuration> doWriterSplit(int readerTaskNumber) {

        List<Configuration> writerSlicesConfigs = this.jobWriter
                .split(readerTaskNumber);
        if (writerSlicesConfigs == null || writerSlicesConfigs.size() <= 0) {
            throw DataETLException.asDataETLException(
                    FrameworkErrorCode.PLUGIN_SPLIT_ERROR,
                    "writer切分的task不能小于等于0");
        }
        LOG.info("DataX Writer.Job [{}] splits to [{}] tasks.",
                this.writerPluginName, writerSlicesConfigs.size());

        return writerSlicesConfigs;
    }

    private List<Configuration> mergeReaderAndWriterTaskConfigs(
            List<Configuration> readerTasksConfigs,
            List<Configuration> writerTasksConfigs) {
        if (readerTasksConfigs.size() != writerTasksConfigs.size()) {
            throw DataETLException.asDataETLException(
                    FrameworkErrorCode.PLUGIN_SPLIT_ERROR,
                    String.format("reader切分的task数目[%d]不等于writer切分的task数目[%d].",
                            readerTasksConfigs.size(), writerTasksConfigs.size())
            );
        }

        List<Configuration> contentConfigs = new ArrayList<Configuration>();
        for (int i = 0; i < readerTasksConfigs.size(); i++) {
            Configuration taskConfig = Configuration.newDefault();
            taskConfig.set(CoreConstant.JOB_READER_NAME,
                    this.readerPluginName);
            taskConfig.set(CoreConstant.JOB_READER_PARAMETER,
                    readerTasksConfigs.get(i));
            taskConfig.set(CoreConstant.JOB_WRITER_NAME,
                    this.writerPluginName);
            taskConfig.set(CoreConstant.JOB_WRITER_PARAMETER,
                    writerTasksConfigs.get(i));

            taskConfig.set(CoreConstant.TASK_ID, i);
            contentConfigs.add(taskConfig);
        }

        return contentConfigs;
    }

    private StandAloneScheduler initStandaloneScheduler(Configuration configuration) {
        AbstractContainerCommunicator containerCommunicator = new StandAloneJobContainerCommunicator(configuration);
        super.setContainerCommunicator(containerCommunicator);

        return new StandAloneScheduler(containerCommunicator);
    }

    @Override
    public void post() {
        this.jobReader.post();
        this.jobWriter.post();
    }

    @Override
    public void destory() {
        this.jobReader.destroy();
        this.jobWriter.destroy();
    }
}
