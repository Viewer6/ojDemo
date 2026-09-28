package com.viewer.common.message.config;


import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AliSmsConfig {

    @Value("${sms.aliyun.endpoint}")
    private String endpoint;

    @Value("${sms.aliyun.accessKeyId}")
    private String accessKeyId;

    @Value("${sms.aliyun.accessKeySecret}")
    private String accessKeySecret;


    @Bean("aliClient")
    public Client client() throws Exception{
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret)
                .setEndpoint(endpoint);  // Endpoint 请参考 https://api.aliyun.com/product/Dypnsapi

        return new Client(config);

    }
}
