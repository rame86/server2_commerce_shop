package com.example.shop.messaging.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.example.shop.config.RabbitMQConfig;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShopEventListener {

    // 지정된 큐를 구독하고, JSON 데이터를 DTO로 자동 변환하여 받음
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveMessage() {
    }

    @RabbitListener(queues = RabbitMQConfig.REPLY_QUEUE_NAME)
    public void replyReceiveMessage() {
    }
}
