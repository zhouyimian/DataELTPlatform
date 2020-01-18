package com.km.data.core;


import com.km.data.common.util.Configuration;
import org.apache.commons.lang.Validate;

/**
 * 执行容器的抽象类，持有该容器全局的配置 configuration
 */
public abstract class AbstractContainer {
    protected Configuration configuration;


    public AbstractContainer(Configuration configuration) {
        Validate.notNull(configuration, "Configuration can not be null.");

        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public abstract void start();

    public abstract void post();

    public abstract void destory();
}
