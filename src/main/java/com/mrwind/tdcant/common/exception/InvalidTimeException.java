package com.mrwind.tdcant.common.exception;

public class InvalidTimeException extends Exception{
    private String msg;
    public InvalidTimeException(String msg) {
        this.msg = msg;
    }
    public InvalidTimeException() {
        this.msg = "无效时效";
    }
    public String getErrMsg() {
        return this.msg;
    }
}
