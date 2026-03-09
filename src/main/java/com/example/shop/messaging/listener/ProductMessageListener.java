package com.example.shop.messaging.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.shop.config.RabbitMQConfig;
import com.example.shop.dto.message.ProductCreatedMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProductMessageListener {

    /**
     * shop.request.queue를 모니터링하다가 메시지가 들어오면 실행됨
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveProductMessage(ProductCreatedMessage message) {
        log.info("메시지 수신 성공!");
        log.info("수신된 상품명: {}", message.title());
        log.info("수신된 가격: {}", message.price());
        
        // 여기서 알림 발송이나 검색 엔진(Elasticsearch) 데이터 업데이트 등의 로직 수행
    }
}
