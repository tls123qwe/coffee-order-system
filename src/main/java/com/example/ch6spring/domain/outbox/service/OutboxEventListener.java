package com.example.ch6spring.domain.outbox.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor

// 주문이 커밋되면 즉시 전송(실시간성)
public class OutboxEventListener {

    private final OutboxSender outboxSender;

    @Async // 별도 스레드에서 실행 -> 사용자는 기다리지 않고 바로 주문 응답을 받음
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 주문 트랜잭션 커밋 후에만 실행
    public void handle(OutboxSavedEvent event) {

        outboxSender.sendById(event.outboxEventId());
    }
}