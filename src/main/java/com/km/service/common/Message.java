package com.km.service.common;

import com.alibaba.fastjson.JSONObject;

public class Message {
    String exception;
    String data;

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
