package com.km.core.job;

import com.km.common.exception.CommonErrorCode;
import com.km.common.exception.DataETLException;
import com.km.common.util.Configuration;
import com.km.core.transport.channel.Channel;
import com.km.core.transport.channel.memory.MemoryChannel;
import com.km.core.util.container.CoreConstant;
import com.km.reader.*;
import com.km.writer.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

public class TaskRunner implements Runnable {
    Configuration configuration;
    Reader.Task reader;
    Writer.Task writer;
    CountDownLatch countDownLatch;

    Channel channel;


    public TaskRunner(Configuration configuration,CountDownLatch countDownLatch) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        this.configuration = configuration;
        this.reader = createReader(configuration);
        this.writer = createWriter(configuration);
        this.channel = createChannel();
        this.countDownLatch = countDownLatch;
    }

    private Channel createChannel() {
        return new MemoryChannel();
    }

    private Reader.Task createReader(Configuration configuration) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String className = configuration.getString(CoreConstant.JOB_READER_PARAMETER+".readerClassPath");
        return (Reader.Task) createTask(className,configuration.getConfiguration(CoreConstant.JOB_READER_PARAMETER));

    }

    private Writer.Task createWriter(Configuration configuration) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String className = configuration.getString(CoreConstant.JOB_WRITER_PARAMETER+".writerClassPath");
        return (Writer.Task) createTask(className,configuration.getConfiguration(CoreConstant.JOB_WRITER_PARAMETER));
    }

    @Override
    public void run() {
        try {
            this.reader.startRead(this.channel);
            this.writer.startWrite(this.channel);
            countDownLatch.countDown();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Object createTask(String className, Configuration configuration) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Class clazz = Class.forName(className);
        Class innerClazz[] = clazz.getDeclaredClasses();
        Constructor constructor = null;
        for(Class cls : innerClazz){
            if(cls.getName().contains("Task")){
                constructor = cls.getConstructor(Configuration.class);
            }
        }
        if(constructor==null){
            throw DataETLException.asDataETLException
                    (CommonErrorCode.RUNTIME_ERROR,"您实现的reader或者writer插件缺少实现Task内部类");
        }
        return constructor.newInstance(configuration);
    }
}
