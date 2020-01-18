package com.km.service.common.exception;

public class serviceException extends Throwable {
    private String errormesaage;
    public serviceException(String errormesaage){
        this.errormesaage = errormesaage;
    }

    public String getErrormesaage() {
        return errormesaage;
    }

    public void setErrormesaage(String errormesaage) {
        this.errormesaage = errormesaage;
    }

    @Override
    public String toString() {
        return "serviceException{" +
                "errormesaage='" + errormesaage + '\'' +
                '}';
    }
}
