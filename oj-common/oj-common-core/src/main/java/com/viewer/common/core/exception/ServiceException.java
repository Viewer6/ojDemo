package com.viewer.common.core.exception;

import com.viewer.common.core.enums.ResultCode;
import lombok.Data;

@Data
public class ServiceException extends RuntimeException{
    private Integer code;
    private String msg;
    public ServiceException(){
        super();
    }

    public ServiceException(ResultCode resultCode){
        super();
        this.code = resultCode.getCode();
        this.msg = resultCode.getMeg();
    }
}
