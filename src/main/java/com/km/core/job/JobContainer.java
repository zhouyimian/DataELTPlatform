package com.km.core.job;


import com.alibaba.fastjson.JSONObject;
import com.km.common.exception.CommonErrorCode;
import com.km.common.exception.DataETLException;
import com.km.common.util.Configuration;
import com.km.core.AbstractContainer;
import com.km.core.util.FrameworkErrorCode;
import com.km.core.util.container.CoreConstant;
import com.km.reader.*;
import com.km.writer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
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
            this.post();

            this.destory();

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
        Configuration readerConfig = Configuration.from(readFile(readerConfigPath));
        Configuration writerConfig = Configuration.from(readFile(writerConfigPath));

        List<JSONObject> readerPlugins = readerConfig.getList("reader",JSONObject.class);
        List<JSONObject> writerPlugins = writerConfig.getList("writer",JSONObject.class);

        String readerPluginClass = null;
        String writerPluginClass = null;

        for(JSONObject readerPluginConfig:readerPlugins){
            if(readerPluginConfig.getString("name").equals(this.readerPluginName)){
                readerPluginClass = readerPluginConfig.getString("classPath");
                break;
            }
        }
        if(readerPluginClass==null){
            throw DataETLException.asDataETLException(CommonErrorCode.CONFIG_ERROR,
                    "配置的reader[ "+ this.readerPluginName +"]不存在，请重新输入reader");
        }
        for(JSONObject writerPluginConfig:writerPlugins){
            if(writerPluginConfig.getString("name").equals(this.writerPluginName)){
                writerPluginClass = writerPluginConfig.getString("classPath");
                break;
            }
        }
        if(writerPluginClass==null){
            throw DataETLException.asDataETLException(CommonErrorCode.CONFIG_ERROR,
                    "配置的writer[ "+ this.writerPluginName +"]不存在，请重新输入writer");
        }
        configuration.set("test","test");
        Configuration readerParamter = configuration.getConfiguration(CoreConstant.DATAX_JOB_CONTENT_READER_PARAMETER);
        Configuration writerParamter = configuration.getConfiguration(CoreConstant.DATAX_JOB_CONTENT_WRITER_PARAMETER);
        readerParamter.set("readerClassPath",readerPluginClass);
        writerParamter.set("writerClassPath",writerPluginClass);


        this.jobReader = (Reader.Job) createJob(readerPluginClass,readerParamter);
        this.jobWriter = (Writer.Job) createJob(writerPluginClass,writerParamter);
    }



    private Object createJob(String className, Configuration configuration) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Class clazz = Class.forName(className);
        Class innerClazz[] = clazz.getDeclaredClasses();
        Constructor constructor = null;
        for(Class cls : innerClazz){
            if(cls.getName().contains("Job")){
               constructor = cls.getConstructor(Configuration.class);
            }
        }
        if(constructor==null){
            throw DataETLException.asDataETLException
                    (CommonErrorCode.RUNTIME_ERROR,"您实现的reader或者writer插件缺少实现Job内部类");
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

    public void schedule() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, InterruptedException {
        int taskNumber = this.configuration.getList(
                CoreConstant.DATAX_JOB_CONTENT).size();

        this.needChannelNumber = Math.min(this.needChannelNumber, taskNumber);

        startAllTask(this.configuration.getListConfiguration(CoreConstant.DATAX_JOB_CONTENT));

    }

    private void startAllTask(List<Configuration> configurations) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(configurations.size());
        this.taskExecutorService = Executors
                .newFixedThreadPool(configurations.size());

        for(Configuration conf:configurations){
            TaskRunner runner = new TaskRunner(conf,countDownLatch);
            taskExecutorService.execute(runner);
        }

        countDownLatch.await();
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

    private static String readFile(String filePath) {
        // 读取txt内容为字符串
        StringBuffer txtContent = new StringBuffer();
        // 每次读取的byte数
        byte[] b = new byte[8 * 1024];
        InputStream in = null;
        try {
            // 文件输入流
            in = new FileInputStream(filePath);
            while (in.read(b) != -1) {
                // 字符串拼接
                txtContent.append(new String(b));
            }
            // 关闭流
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return txtContent.toString();
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
