package com.viewer.common.core.service;

import com.viewer.common.core.domain.Result;
import com.viewer.common.core.emuns.ResultCode;

public class BaseService {
//    if(result > 1){
//        return Result.success(null);
//    }
//    return Result.error(null);
    public Result<Void> getResult(int result){
        return result == 1 ? Result.success(null) : Result.fail(ResultCode.FAILED, null);
    }

//    if(result > 1){
//        return Result.success(null);
//    }
//    return Result.error(null);
    public Result<Void> getResult(boolean result){
        return result ? Result.success(null) : Result.fail(ResultCode.FAILED, null);
    }
}
