package com.viewer.judge.callback;

import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.viewer.common.core.enums.CodeRunStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Getter
@Setter
@Slf4j
public class DockerStartResultCallback extends ExecStartResultCallback {

    private CodeRunStatus codeRunStatus;  //记录执行成功还是失败

    private String errorMessage;

    private String message;

    @Override
    public void onNext(Frame frame) {
        StreamType streamType = frame.getStreamType();
        // 容器内输出是 UTF-8，必须显式指定字符集：
        // 用 new String(bytes) 会走 JVM 默认编码（Windows 下通常是 GBK），中文报错会变乱码
        String payload = new String(frame.getPayload(), StandardCharsets.UTF_8);
        if (StreamType.STDERR.equals(streamType)) {
            // 编译错误、异常栈可能分多个 Frame 下发，必须拼接而不是覆盖，否则只剩最后一段
            errorMessage = StrUtil.isEmpty(errorMessage) ? payload : errorMessage + payload;
            codeRunStatus = CodeRunStatus.FAILED;
        } else {
            if (StrUtil.isNotEmpty(payload)) {
                // 用户程序的标准输出同样可能分多个 Frame，拼接后才和期望输出一致
                message = StrUtil.isEmpty(message) ? payload : message + payload;
            }
            codeRunStatus = CodeRunStatus.SUCCEED;
        }
        super.onNext(frame);
    }
}
