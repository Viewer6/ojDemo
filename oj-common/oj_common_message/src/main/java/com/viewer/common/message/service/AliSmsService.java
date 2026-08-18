package com.viewer.common.message.service;

import com.alibaba.fastjson2.JSON;
import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.models.RuntimeOptions;
import com.viewer.common.message.config.AliSmsConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class AliSmsService {

    @Autowired
    private Client aliClient;

    //业务配置
    @Value("${sms.aliyun.templateCode:}")
    private String templateCode;

    @Value("${sms.aliyun.sign-name:}")
    private String singName;

    /**
     * 发送短信验证码
     *
     * @param phone
     * @param code
     */
    public boolean sendMobileCode(String phone, String code) {
        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        params.put("min", "5"); // 验证码时效
        return sendTempMessage(phone, singName, templateCode, params);
    }


    /**
     * 发送模板消息
     *
     * @param phone
     * @param singName
     * @param templateCode
     * @param params
     */
    public boolean sendTempMessage(String phone, String singName, String templateCode,
                                Map<String, String> params) {
        SendSmsVerifyCodeRequest sendSmsVerifyCodeRequest = new SendSmsVerifyCodeRequest()
                .setSignName(singName)
                .setTemplateCode(templateCode)
                .setPhoneNumber(phone)
                .setTemplateParam(JSON.toJSONString(params));
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            assert aliClient != null;
            SendSmsVerifyCodeResponse resp = aliClient.sendSmsVerifyCodeWithOptions(sendSmsVerifyCodeRequest, runtime);
            log.info("阿里云短信发送响应: {}", new Gson().toJson(resp));
            // 判断业务返回码
            if ("OK".equals(resp.getBody().getCode())) {
                log.info("短信发送成功,phone:{}", phone);
                return true;
            } else {
                log.error("短信发送业务失败 code:{},msg:{}", resp.getBody().getCode(), resp.getBody().getMessage());
                return false;
            }
        } catch (TeaException error) {
            log.error("阿里云sms TeaException异常，msg={}", error.getMessage(), error);
            // 安全获取Recommend，防止getData为null
            if(error.getData() != null){
                Object recommend = error.getData().get("Recommend");
                if(recommend != null){
                    log.error("阿里云诊断地址：{}", recommend);
                }
            }
        } catch (Exception e) {
            log.error("阿里云短信调用未知异常", e);
        }
        return false;
    }
}