package com.km.data.core;


import com.km.data.common.util.Configuration;
import com.km.data.core.job.JobContainer;
import com.km.data.core.util.container.CoreConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Engine {
    private static final Logger LOG = LoggerFactory.getLogger(Engine.class);
    private static String RUNTIME_MODE;

    public void start(Configuration configuration) {
        configuration.set(CoreConstant.DATAX_CORE_CONTAINER_JOB_MODE, RUNTIME_MODE);
        AbstractContainer container = new JobContainer(configuration);

        container.start();
    }

}
