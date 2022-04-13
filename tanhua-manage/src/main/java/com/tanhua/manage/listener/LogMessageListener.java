package com.tanhua.manage.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tanhua.manage.domain.Log;
import com.tanhua.manage.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * rocket mq 日志的消费者
 */
//@Component
@RocketMQMessageListener(topic = "tanhua_log",consumerGroup = "tanhua-log-consumer")
@Slf4j
public class LogMessageListener implements RocketMQListener<String> {

    @Autowired
    private LogService logService;

    @Override
    public void onMessage(String message) {
        JSONObject map = JSON.parseObject(message);
        log.info("日志消费者接收到的消息：{}",message);
        // 取消息内容
        Integer userId = (Integer) map.get("userId");
        String type = (String) map.get("type");
        String date = (String) map.get("date");
        // 构建log对象
        Log log = new Log();
        log.setLogTime(date);
        log.setType(type);
        log.setUserId(userId.longValue());
        log.setEquipment("Huawei mate 40");
        log.setPlace("深圳");
        log.setCreated(new Date());
        // 调用service存入log表
        logService.add(log);
    }
}
