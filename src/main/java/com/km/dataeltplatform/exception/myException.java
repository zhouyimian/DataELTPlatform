package com.km.dataeltplatform.exception;

public class myException {
    private String errormesaage;
    public myException(String errormesaage){
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
        return "myException{" +
                "errormesaage='" + errormesaage + '\'' +
                '}';
    }
}
