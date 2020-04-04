package com.km.data.plugin;

import com.km.data.common.annotations.Field;
import com.km.data.common.util.Configuration;
import com.km.data.core.transport.channel.Channel;


public class filterNullValuePlugin extends Plugin {


    @Field(fieldName = "字段名", desc = "进行空值过滤的字段名称",necessary = true)
    private String fieldName;

    public filterNullValuePlugin(Configuration configuration) {
        super(configuration);
    }


    @Override
    public void process(Channel channel) {
        for (int i = 0; i < channel.size(); i++) {
            if (channel.get(i).getColumnValue(fieldName) == null) {
                channel.remove(i);
                i--;
            }
        }
    }
}
