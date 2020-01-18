package com.km.data.plugin;

import com.km.data.common.util.Configuration;
import com.km.data.core.transport.channel.Channel;

public abstract class Plugin {
    private Configuration configuration;

    public Plugin(){};

    public Plugin(Configuration configuration){
        this.configuration = configuration;
    }

    public abstract void process(Channel channel);
}
