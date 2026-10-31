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
        super(resultCode.getMeg());
        this.code = resultCode.getCode();
        this.msg = resultCode.getMeg();
    }

    /**
     * 业务异常，允许在枚举文案之外补充更具体的提示
     */
    public ServiceException(ResultCode resultCode, String msg){
        super(msg);
        this.code = resultCode.getCode();
        this.msg = msg;
    }
}
