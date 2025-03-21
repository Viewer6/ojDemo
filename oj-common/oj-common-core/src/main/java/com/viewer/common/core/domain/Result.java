package com.viewer.common.core.domain;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String errMeg;
    private T data;

    public static <T> Result success(T data){
        Result<T> result = new Result<>();

        result.setCode(1000);
        result.setErrMeg("");
        result.setData(data);

        return result;
    }

    public static <T> Result fail(Integer code, String message){
        Result<T> result = new Result<>();

        result.setCode(code);
        result.setErrMeg(message);

        return result;
    }

    public static <T> Result fail(Integer code, String message, T data){
        Result<T> result = new Result<>();

        result.setCode(code);
        result.setErrMeg(message);
        result.setData(data);

        return result;
    }


}