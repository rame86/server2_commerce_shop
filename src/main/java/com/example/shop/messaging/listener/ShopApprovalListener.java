package com.example.shop.messaging.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.shop.config.RabbitMQConfig;
import com.example.shop.dto.message.ShopApprovalMessage;
import com.example.shop.entity.Approval;
import com.example.shop.entity.enums.ProductCategory;
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
                .requesterId(message.requesterId())
                .requesterName(message.requesterName())
                .goodsName(message.goodsName())
                .goodsType(ProductCategory.valueOf(message.goodsType()))
                .description(message.description())
                .price(message.price())             // ✅ BigDecimal 유지
                .color(message.color())             // ✅ 신규 (DB: color)
                .size(message.size())               // ✅ 신규 (DB: size)
                .stockQuantity(message.stockQuantity()) // ✅ stock → stockQuantity
                .imageUrl(message.imageUrl())
                .build();

        shopApprovalRepository.save(approval);
        log.info("승인 요청 DB 저장 완료! approvalId: {}", approval.getApprovalId());
    }

    // ✅ ShopEventListener 통합 — 응답 메시지 수신 (REPLY_QUEUE_NAME 전용)
    @RabbitListener(queues = RabbitMQConfig.REPLY_QUEUE_NAME)
    public void replyReceiveMessage(Object message) {
        // TODO: 결제 결과, 외부 서비스 콜백 등 응답 처리 로직 구현
        log.info("응답 메시지 수신: {}", message);
    }
}