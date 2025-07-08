package com.viewer.common.core.exception;

import com.viewer.common.core.emuns.ResultCode;
import lombok.Data;

@Data
public class UserException extends RuntimeException{
    private Integer code;
    private String msg;
    public UserException(){
        super();
    }

    public UserException(ResultCode resultCode){
        super();
        this.code = resultCode.getCode();
        this.msg = resultCode.getMeg();
    }
}
