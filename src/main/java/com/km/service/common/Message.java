package com.km.service.common;

import com.alibaba.fastjson.JSONObject;

public class Message {
    String exception;
    JSONObject data;

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }
}
