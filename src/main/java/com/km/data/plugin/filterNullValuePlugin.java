package com.km.data.plugin;

import com.km.data.common.annotations.Field;
import com.km.data.common.util.Configuration;
import com.km.data.core.transport.channel.Channel;


public class filterNullValuePlugin extends Plugin {

    @Field(fieldName = "field1", desc = "这个是想要进行空值过滤的字段名称",necessary = true)
    private String fieldName;

    @Field(fieldName = "field2", desc = "这个是想要进行空值过滤的字段名称",necessary = false)
    private String test;

    public filterNullValuePlugin(Configuration configuration) {
        super(configuration);
    }


    @Override
    public void process(Channel channel) {
        int size = channel.size();
        for (int i = 0; i < size; i++) {
            if (channel.get(i).getColumnValue(fieldName) == null) {
                channel.remove(i);
            }
        }
    }
}
