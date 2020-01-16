package com.km.core.job;


import com.km.common.exception.DataETLException;
import com.km.common.util.Configuration;
import com.km.core.AbstractContainer;
import com.km.core.util.FrameworkErrorCode;
import com.km.core.util.container.CoreConstant;
import com.km.reader.*;
import com.km.writer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * job实例运行在jobContainer容器中，它是所有任务的master，负责初始化、拆分、调度、运行、回收、监控和汇报
 * 但它并不做实际的数据同步操作
 */
public class JobContainer extends AbstractContainer {
    private static final Logger LOG = LoggerFactory
            .getLogger(JobContainer.class);

    private String readerPluginName;

    private String writerPluginName;

    /**
     * reader和writer jobContainer的实例
     */
    private Reader.Job jobReader;

    private Writer.Job jobWriter;


    private Integer needChannelNumber;

    private ExecutorService taskExecutorService;


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
    public void init(){

        this.readerPluginName = configuration.getString(CoreConstant.DATAX_JOB_CONTENT_READER_NAME);
        this.writerPluginName = configuration.getString(CoreConstant.DATAX_JOB_CONTENT_WRITER_NAME);

        //这里得根据plugname获取到类的路径进行初始化，这里只是简单创建下
        this.jobReader = new MongoDBReader.Job(configuration.getConfiguration(CoreConstant.DATAX_JOB_CONTENT_READER_PARAMETER));
        this.jobWriter = new MongoDBWriter.Job(configuration.getConfiguration(CoreConstant.DATAX_JOB_CONTENT_WRITER_PARAMETER));
    }
    private int split() {
        this.needChannelNumber = this.configuration.getInt(CoreConstant.DATAX_JOB_SETTING_SPEED_CHANNEL);
        //该方法通过读取配置文件，以及channel的数量，进行任务的切分，生成对应的configuration，
        // 在这里使用的是mysqlreader，因此切分后的configuration中，不同的地方仅仅只是sql的范围不同。
        //底层的split得自己去实现
        List<Configuration> readerTaskConfigs = this
                .doReaderSplit(this.needChannelNumber);
        int taskNumber = readerTaskConfigs.size();
        List<Configuration> writerTaskConfigs = this
                .doWriterSplit(taskNumber);


        /**
         * 输入是reader和writer的parameter list，输出是content下面元素的list
         */
        List<Configuration> contentConfig = mergeReaderAndWriterTaskConfigs(
                readerTaskConfigs, writerTaskConfigs);


        this.configuration.set(CoreConstant.DATAX_JOB_CONTENT, contentConfig);

        return contentConfig.size();
    }

    public void schedule(){
        int taskNumber = this.configuration.getList(
                CoreConstant.DATAX_JOB_CONTENT).size();

        this.needChannelNumber = Math.min(this.needChannelNumber, taskNumber);

        startAllTask(this.configuration.getListConfiguration(CoreConstant.DATAX_JOB_CONTENT));

    }

    private void startAllTask(List<Configuration> configurations) {
        this.taskExecutorService = Executors
                .newFixedThreadPool(configurations.size());

        for(Configuration conf:configurations){
            TaskRunner runner = new TaskRunner(conf);
            taskExecutorService.execute(runner);
        }
        this.taskExecutorService.shutdown();
    }


    public List<Configuration> doReaderSplit(int adviceNumber){
        List<Configuration> readerSlicesConfigs = this.jobReader.split(adviceNumber);
        if(readerSlicesConfigs==null||readerSlicesConfigs.size()==0){
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
}
