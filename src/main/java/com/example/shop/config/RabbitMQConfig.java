package com.example.shop.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration // 1. @Configuration 어노테이션 누락 확인
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "msa.direct.exchange";
    public static final String QUEUE_NAME = "shop.request.queue";
    public static final String ROUTING_KEY = "shop.request";

    public static final String REPLY_ROUTING_KEY = "reply.shop.request";
    public static final String REPLY_QUEUE_NAME = "reply.shop.request.queue";

    public static final String SHOP_REQ_ROUTING_KEY = "shop.request";

    // 결제관련
    public static final String SHOP_PAY_REPLY_QUEUE = "shop.pay.reply.queue";
    public static final String SHOP_PAY_REPLY_ROUTING_KEY = "shop.pay.reply";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue requestQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Queue replyQueue() {
        return new Queue(REPLY_QUEUE_NAME, true);
    }

    @Bean
    public Queue shopPayReplyQueue() {
        return new Queue(SHOP_PAY_REPLY_QUEUE, true);
    }

    @Bean
    public Binding shopPayReplyBinding(@Qualifier("shopPayReplyQueue") Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(SHOP_PAY_REPLY_ROUTING_KEY);
    }

    /**
     * 요청 큐 바인딩
     */
    @Bean
    public Binding requestBinding(@Qualifier("requestQueue") Queue queue, DirectExchange exchange) {
        log.info("Binding Queue: {} to Exchange: {} with Routing Key: {}", QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    /**
     * 응답(Reply) 큐 바인딩
     */
    @Bean
    public Binding replyBinding(@Qualifier("replyQueue") Queue queue, DirectExchange exchange) {
        log.info("Binding Reply Queue: {} to Exchange: {} with Routing Key: {}", REPLY_QUEUE_NAME, EXCHANGE_NAME,
                REPLY_ROUTING_KEY);
        return BindingBuilder.bind(queue).to(exchange).with(REPLY_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        // LocalDateTime 같은 Java 8 날짜 타입을 인식할 수 있게 해줌
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    // RabbitTemplate에 변환기 설정 주입
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        log.info("Configuring RabbitTemplate with ConnectionFactory and MessageConverter");
        // 클래스명(ConnectionFactory)이 아닌 주입받은 변수명(connectionFactory)을 사용
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        // 메시지 미전달 시 확인을 위한 설정 (선택)
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }
}