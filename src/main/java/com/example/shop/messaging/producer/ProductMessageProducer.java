package com.example.shop.messaging.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.example.shop.config.RabbitMQConfig;
import com.example.shop.dto.message.ShopApprovalMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendProductCreatedEvent(ShopApprovalMessage message) {
        log.info("========== Producer 호출됨! ==========");
        log.info("전송 데이터: {}", message);
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY,
                    message);
            log.info("========== RabbitMQ 전송 완료! ==========");
        } catch (Exception e) {
            log.error("========== 전송 중 에러 발생! ==========", e);
        }
    }
}