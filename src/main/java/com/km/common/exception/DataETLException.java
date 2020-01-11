package com.km.common.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

public class DataETLException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private ErrorCode errorCode;

    public DataETLException(ErrorCode errorCode, String errorMessage) {
        super(errorCode.toString() + " - " + errorMessage);
        this.errorCode = errorCode;
    }

    private DataETLException(ErrorCode errorCode, String errorMessage, Throwable cause) {
        super(errorCode.toString() + " - " + getMessage(errorMessage) + " - " + getMessage(cause), cause);

        this.errorCode = errorCode;
    }

    public static DataETLException asDataETLException(ErrorCode errorCode, String message) {
        return new DataETLException(errorCode, message);
    }

    public static DataETLException asDataETLException(ErrorCode errorCode, String message, Throwable cause) {
        if (cause instanceof DataETLException) {
            return (DataETLException) cause;
        }
        return new DataETLException(errorCode, message, cause);
    }

    public static DataETLException asDataETLException(ErrorCode errorCode, Throwable cause) {
        if (cause instanceof DataETLException) {
            return (DataETLException) cause;
        }
        return new DataETLException(errorCode, getMessage(cause), cause);
    }

    public ErrorCode getErrorCode() {
        return this.errorCode;
    }

    private static String getMessage(Object obj) {
        if (obj == null) {
            return "";
        }

        if (obj instanceof Throwable) {
            StringWriter str = new StringWriter();
            PrintWriter pw = new PrintWriter(str);
            ((Throwable) obj).printStackTrace(pw);
            return str.toString();
            // return ((Throwable) obj).getMessage();
        } else {
            return obj.toString();
        }
    }
}
