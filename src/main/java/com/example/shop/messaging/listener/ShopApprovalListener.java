package com.example.shop.messaging.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.shop.config.RabbitMQConfig;
import com.example.shop.dto.message.ShopApprovalMessage;
import com.example.shop.entity.Approval;
import com.example.shop.repository.ShopApprovalRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShopApprovalListener {

    private final ShopApprovalRepository shopApprovalRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveApprovalMessage(ShopApprovalMessage message) {
        log.info("승인 요청 메시지 수신: {}", message.goodsName());

        Approval approval = Approval.builder()
                .goodsId(message.goodsId())
                .requesterId(message.requesterId())
                .requesterName(message.requesterName())
                .goodsName(message.goodsName())
                .goodsType(message.goodsType())
                .description(message.description())
                .price(message.price())
                .stock(message.stock())
                .imageUrl(message.imageUrl())
                .build();

        shopApprovalRepository.save(approval);
        log.info("승인 요청 DB 저장 완료! approvalId: {}", approval.getApprovalId());
    }
}