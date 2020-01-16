package com.km.core.transport.channel.memory;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.km.common.element.Record;
import com.km.core.transport.channel.Channel;

import java.util.ArrayList;
import java.util.List;


/**
 * 内存Channel的具体实现，底层其实是一个ArrayBlockingQueue
 *
 */
public class MemoryChannel extends Channel {
    List<Record> list = new ArrayList<>();
    public void add(Record record){
        list.add(record);
    }

    @Override
    public Record remove() {
        if(list.size()!=0)
            return list.remove(0);
        return null;
    }
}
