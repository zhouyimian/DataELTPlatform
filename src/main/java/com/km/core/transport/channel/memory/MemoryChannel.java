package com.km.core.transport.channel.memory;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.km.core.transport.channel.Channel;


/**
 * 内存Channel的具体实现，底层其实是一个ArrayBlockingQueue
 *
 */
public class MemoryChannel extends Channel {
    JSONArray jsonArray = new JSONArray();

    public void add(JSONObject object){
        jsonArray.add(object);
    }

    @Override
    public JSONObject remove() {
        if(jsonArray.size()!=0)
            return (JSONObject) jsonArray.remove(0);
        return null;
    }
}
