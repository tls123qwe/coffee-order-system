package com.example.ch6spring.domain.outbox.service;

import com.example.ch6spring.domain.order.entity.Order;
import com.example.ch6spring.domain.outbox.client.OrderDataPayload;
import com.example.ch6spring.domain.outbox.entity.OutboxEvent;
import com.example.ch6spring.domain.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor

// 주문 트랜잭션 안에서 Outbox 이벤트를 저장
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    // 이미 진행 중인 주문 트랜잭션에 참여
    @Transactional
    public void saveOrderEvent(Order order) {

        // 보낼 데이터를 JSON 문자열로 변환
        String payload = objectMapper.writeValueAsString(OrderDataPayload.from(order));
        // outbox 테이블에 Pending 상태로 저장
        OutboxEvent outbox = outboxEventRepository.save(OutboxEvent.create(order.getId(), payload));
        // 전송 신호 발생
        eventPublisher.publishEvent(new OutboxSavedEvent(outbox.getId()));
    }
}