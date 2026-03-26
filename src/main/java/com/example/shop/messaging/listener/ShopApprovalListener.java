package com.example.shop.messaging.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.shop.config.RabbitMQConfig;
import com.example.shop.dto.message.ShopApprovalMessage;
import com.example.shop.entity.Approval;
import com.example.shop.entity.enums.ProductCategory;
import com.example.shop.repository.ShopApprovalRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShopApprovalListener {

    private final ShopApprovalRepository shopApprovalRepository;
    private final com.example.shop.repository.ProductRepository productRepository; // 추가

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    @Transactional
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
    public void replyReceiveMessage(java.util.Map<String, Object> message) {
        log.info("응답 메시지 수신: {}", message);

        try {
            // Core 서비스에서 ShopResultDTO 등의 형태로 응답이 왔을 때 "CONFIRMED" 상태인 경우 상품을 활성화
            if (message.containsKey("status") && "CONFIRMED".equals(message.get("status"))) {
                Object goodsIdObj = message.get("goodsId"); // core쪽 dto의 goodsId = product_id

                if (goodsIdObj != null) {
                    Long productId = Long.valueOf(goodsIdObj.toString());
                    
                    com.example.shop.entity.Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new IllegalArgumentException("승인 처리 대상 상품을 찾을 수 없습니다. ID: " + productId));
                    
                    product.activateProduct(); // is_active = true로 업데이트
                    productRepository.save(product); // 명시적 저장
                    
                    log.info(">>>> Core 서비스 승인 완료! [Product ID: {}] 의 노출 상태(isActive)를 true로 변경했습니다.", productId);
                } else {
                    log.warn("승인 응답 메시지에 goodsId(productId)가 존재하지 않습니다.");
                }
            }
        } catch (Exception e) {
            log.error("응답 메시지 승인 업데이트 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}