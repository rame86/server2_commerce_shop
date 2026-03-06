package com.example.shop.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration // 1. @Configuration 어노테이션 누락 확인
public class RabbitMQConfig {
    
    public static final String EXCHANGE_NAME = "msa.direct.exchange";
    public static final String ROUTING_KEY = "shop.request";
    public static final String QUEUE_NAME = "shop.request.queue";
    public static final String REPLY_ROUTING_KEY = "reply.shop.request";
    public static final String REPLY_QUEUE_NAME = "reply.shop.request.queue";
    
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

    /**
     * 요청 큐 바인딩
     */
    @Bean
    public Binding requestBinding(@Qualifier("requestQueue") Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    /**
     * 응답(Reply) 큐 바인딩
     */
    @Bean
    public Binding replyBinding(@Qualifier("replyQueue") Queue queue, DirectExchange exchange) {
               return BindingBuilder.bind(queue).to(exchange).with(REPLY_ROUTING_KEY);
    }

  @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();}
}