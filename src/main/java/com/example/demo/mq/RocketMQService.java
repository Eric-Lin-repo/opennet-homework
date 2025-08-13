package com.example.demo.mq;

import com.example.demo.config.RocketMQTopicConfig;
import com.example.demo.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RocketMQService {

    private final RocketMQTemplate rocketMQTemplate;

    public void send(NotificationResponse notification) {
        try {
            SendResult sendResult = rocketMQTemplate.syncSend(RocketMQTopicConfig.NOTIFICATION_TOPIC, notification);

            log.info("success to send message - ID: {}, MessageId: {}, Status: {}, Queue: {}",
                    notification.getId(), sendResult.getMsgId(), sendResult.getSendStatus(), sendResult.getMessageQueue());

        } catch (Exception e) {
            log.error("Fail to send message - ID: {}, 錯誤: {}",
                    notification.getId(), e.getMessage());
        }
    }
}