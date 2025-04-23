package com.viewer.common.core.exception;

import com.viewer.common.core.emuns.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class QuestionException extends RuntimeException{

    private Integer code;
    private String msg;
    public QuestionException(){
        super();
    }

    public QuestionException(ResultCode resultCode){
        super();
        this.code = resultCode.getCode();
        this.msg = resultCode.getMeg();
    }
}
