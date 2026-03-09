package com.example.shop.messaging.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.example.shop.config.RabbitMQConfig;
import com.example.shop.dto.message.ProductCreatedMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendProductCreatedEvent(ProductCreatedMessage message) {
        System.out.println("========== Producer 호출됨! ==========");
        System.out.println("전송 데이터: " + message);
        try {
            // [에러 수정] 클래스명(RabbitTemplate)이 아닌 변수명(rabbitTemplate)으로 호출
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY,
                    message);
            System.out.println("========== RabbitMQ 전송 완료! ==========");
        } catch (Exception e) {
            System.out.println("========== 전송 중 에러 발생! ==========");
            e.printStackTrace();
        }
    }
}