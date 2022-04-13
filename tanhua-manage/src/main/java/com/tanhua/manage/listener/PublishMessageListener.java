package com.tanhua.manage.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tanhua.commons.templates.HuaWeiUGCTemplate;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.dubbo.api.PublishApi;
import com.tanhua.manage.domain.Log;
import com.tanhua.manage.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * rocket mq 动态自动审核的消费者
 */
@Component
@RocketMQMessageListener(topic = "tanhua-publish",consumerGroup = "tanhua-publish-consumer")
@Slf4j
public class PublishMessageListener implements RocketMQListener<String> {

    @Reference
    private PublishApi publishApi;

    @Autowired
    private HuaWeiUGCTemplate huaWeiUGCTemplate;

    @Override
    public void onMessage(String publishId) {
        log.info("开始动态审核: {}", publishId);
        //1. 查询动态的信息
        Publish publish = publishApi.findById(publishId);
        //2. 调用审核文本内容
        Integer state= 2;
        if(StringUtils.isNotBlank(publish.getTextContent())) {
            // 文本审核 通过
            boolean textCheckResult = huaWeiUGCTemplate.textContentCheck(publish.getTextContent());
            log.info("文本审核结果: {},{}",publishId,textCheckResult);
            if (textCheckResult) {
                // 图片也通过
                boolean imageCheckResult = huaWeiUGCTemplate.imageContentCheck(publish.getMedias().toArray(new String[]{}));
                log.info("图片审核结果: {},{}",publishId,imageCheckResult);
                if(imageCheckResult){
                    state=1;
                }
            }
        }
        //5. 更新状态
        publish.setState(state);
        publishApi.updateState(publish);
        log.info("更新动态的审核结果: {},{}",publishId,state);
    }
}
