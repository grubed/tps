package com.test.tps.common;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 * Created by CL-J on 2019/2/18.
 */

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Result<T> {

    public static final Integer CODE_SUCCESS = 0;
    public static final Integer CODE_FAILED = 1;
    public static final Integer CODE_ERROR = -1;
    public static final Integer CODE_HEADER_ERROR = -2;

    private Integer code;
    private String msg;
    private T data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static Result getSuccess() {
        Result result = new Result();
        result.code = CODE_SUCCESS;
        result.msg = "success";
        return result;
    }

    public static Result getSuccess(Object obj) {
        Result result = new Result();
        result.code = CODE_SUCCESS;
        result.msg = "success";
        result.data = obj;
        return result;
    }

    public static Result getFail(String msg) {
        Result result = new Result();
        result.code = CODE_FAILED;
        result.msg = msg;
        return result;
    }
    public static Result getError(String msg) {
        Result result = new Result();
        result.code = CODE_ERROR;
        result.msg = msg;
        return result;
    }
    public static Result getHeaderFail(String msg) {
        Result result = new Result();
        result.code = CODE_HEADER_ERROR;
        result.msg = msg;
        return result;
    }

}

