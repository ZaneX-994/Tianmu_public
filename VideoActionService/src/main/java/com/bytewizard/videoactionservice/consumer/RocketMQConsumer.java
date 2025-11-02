package com.bytewizard.videoactionservice.consumer;

import com.alibaba.fastjson.JSON;
import com.bytewizard.common.domain.dto.SendBulletRequest;
import com.bytewizard.videoactionservice.service.BulletService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RocketMQMessageListener(topic = "tianmu-topic", consumerGroup = "tianmu-consumer-group")
public class RocketMQConsumer implements RocketMQListener<String> {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private BulletService bulletService;


    @Override
    public void onMessage(String message) {

        SendBulletRequest request = JSON.parseObject(message, SendBulletRequest.class);
        System.out.println("收到消息: " + message);

        if (bulletService.bulletExists(request.getBulletId())) {
            return;
        }

        try {
            bulletService.saveBullet(request);
        } catch (Exception e) {
            log.error("保存到MySQL失败，消息ID: {}", request.getBulletId(), e);
            throw new RuntimeException("MySQL保存失败", e);
        }
    }
}
