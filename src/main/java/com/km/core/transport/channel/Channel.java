package com.km.core.transport.channel;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public abstract class Channel {
    JSONArray jsonArray;

    public abstract void add(JSONObject object);

    public abstract JSONObject remove();


}
