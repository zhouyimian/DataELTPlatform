package com.km.core;


import com.km.common.util.Configuration;
import com.km.core.job.JobContainer;
import com.km.core.util.container.CoreConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Engine {
    private static final Logger LOG = LoggerFactory.getLogger(Engine.class);
    private static String RUNTIME_MODE;
    public void start(Configuration configuration){
        //DataX在这里先通过配置文件绑定数据格式，待做
        boolean isJob = !("taskGroup".equalsIgnoreCase(configuration
                .getString(CoreConstant.DATAX_CORE_CONTAINER_MODEL)));
        AbstractContainer container = null;
        if(isJob){
            configuration.set(CoreConstant.DATAX_CORE_CONTAINER_JOB_MODE, RUNTIME_MODE);
            container = new JobContainer(configuration);
        }else{

        }

        container.start();
    }

}
