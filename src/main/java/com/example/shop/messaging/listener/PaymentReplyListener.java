package com.example.shop.messaging.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.shop.dto.message.PaymentResponseDTO;
import com.example.shop.entity.Order;
import com.example.shop.entity.OrderStatus;
import com.example.shop.repository.CartRepository;
import com.example.shop.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentReplyListener {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    @RabbitListener(queues = "shop.pay.reply.queue")
    @Transactional
    public void handlePaymentReply(PaymentResponseDTO replyEvent) {
        log.info(">>>> [결제 응답 수신] OrderID: {}, 상태: {}, 메시지: {}",
                replyEvent.getOrderId(), replyEvent.getStatus(), replyEvent.getMessage());

        try {
            Long orderId = Long.parseLong(replyEvent.getOrderId());
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다. ID: " + orderId));

            // 결제 성공 시 처리 (결제 서비스 status: COMPLETE)
            if ("COMPLETE".equals(replyEvent.getStatus())) {
                order.updateStatus(OrderStatus.PAID);
                log.info(">>>> [주문 성공] OrderID: {} 상태 PAID로 변경", orderId);

                // 장바구니 비우기
                cartRepository.findByMemberId(order.getMemberId()).ifPresent(cart -> {
                    cartRepository.delete(cart);
                    log.info(">>>> [장바구니 비우기 완료] MemberID: {}", order.getMemberId());
                });

            } else {
                // 결제 실패 시 주문 취소 처리
                order.updateStatus(OrderStatus.CANCELLED);
                log.warn(">>>> [결제 실패] OrderID: {} 사유: {} → 주문 취소 처리됨",
                        orderId, replyEvent.getMessage());
            }

        } catch (Exception e) {
            log.error(">>>> [결제 응답 처리 중 오류 발생] : {}", e.getMessage());
            throw e;
        }
    }
}