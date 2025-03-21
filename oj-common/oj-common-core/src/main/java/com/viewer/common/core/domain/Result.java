package com.viewer.common.core.domain;

import com.viewer.common.core.emuns.ResultCode;
import lombok.Data;

// 统一返回结果
@Data
public class Result<T> {
    private int code;
    private String errMeg;
    private T data;

    public static  <T> Result<T> success(T data){
        Result<T> result = new Result<>();

        result.setCode(ResultCode.SUCCESS.getCode());
        result.setErrMeg(ResultCode.SUCCESS.getMeg());
        result.setData(data);

        return result;
    }
    public static  <T>  Result<T> error(T data){
        Result<T> result = new Result<>();

        result.setCode(ResultCode.ERROR.getCode());
        result.setErrMeg(ResultCode.ERROR.getMeg());
        result.setData(data);

        return result;
    }

    public static  <T>  Result<T> fail(ResultCode failedLogin) {
        return Result.fail(failedLogin, null);
    }

    public static  <T>  Result<T> fail(ResultCode failedLogin, T data) {
        Result<T> result = new Result<>();

        result.setCode(failedLogin.getCode());
        result.setErrMeg(failedLogin.getMeg());
        result.setData(data);

        return result;
    }
}