package com.km.service.common;

import com.alibaba.fastjson.JSONObject;
import com.km.service.common.exception.serviceException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice("com.km.service")
public class UnifiedExceptionHandler {

    @ExceptionHandler(serviceException.class)
    public Object handleBusinessException(serviceException exception){
        Message message = new Message();
        message.setException(exception.getErrormesaage());
        return JSONObject.toJSON(message);
    }
}
