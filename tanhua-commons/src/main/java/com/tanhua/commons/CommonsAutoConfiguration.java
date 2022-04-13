package com.tanhua.commons;

import com.tanhua.commons.properties.*;
import com.tanhua.commons.templates.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.crypto.interfaces.PBEKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * 自动配置类
 * 配置工具类 template
 */
@Configuration
@EnableConfigurationProperties({
        SmsProperties.class,
        OssProperties.class,
        FaceProperties.class
        , HuanXinProperties.class,
        HuaWeiUGCProperties.class})
public class CommonsAutoConfiguration {

    /**
     * 创建发送短信的工具类，进入容器, 名称=方法名 smsTemplate
     *
     * @param smsProperties
     * @return
     * @Autowired smsTemplate
     */
    @Bean
    public SmsTemplate smsTemplate(SmsProperties smsProperties) {
        SmsTemplate smsTemplate = new SmsTemplate(smsProperties);
        smsTemplate.init();
        return smsTemplate;
    }

    /**
     * 阿里云存储操作模板
     *
     * @param ossProperties
     * @return
     */
    @Bean
    public OssTemplate ossTemplate(OssProperties ossProperties) {
        return new OssTemplate(ossProperties);
    }

    @Bean
    public FaceTemplate faceTemplate(FaceProperties faceProperties) {
        return new FaceTemplate(faceProperties);
    }

    @Bean
    public HuanXinTemplate huanXinTemplate(HuanXinProperties huanXinProperties) {
        return new HuanXinTemplate(huanXinProperties);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public HuaWeiUGCTemplate huaWeiUGCTemplate(HuaWeiUGCProperties properties) {
        return new HuaWeiUGCTemplate(properties);
    }
}
