package com.example.ch6spring.domain.outbox.service;

import com.example.ch6spring.domain.outbox.client.DataPlatformClient;
import com.example.ch6spring.domain.outbox.client.OrderDataPayload;
import com.example.ch6spring.domain.outbox.entity.OutboxEvent;
import com.example.ch6spring.domain.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor

// outbox 이벤트를 실제로 외부 플랫폼에 전송
// 즉시 전송과 재전송 모두 이 클래스에서
public class OutboxSender {

    private static final int MAX_RETRY = 5;             // 최대 재시도 횟수
    private static final int RETRY_DELAY_SECONDS = 10;  // 생성 후 이 시간이 지난 것만 재전송
    private static final int RETRY_BATCH_SIZE = 100;    // 한 번에 재전송할 최대 개수

    private final OutboxEventRepository outboxEventRepository;
    private final DataPlatformClient dataPlatformClient;
    private final ObjectMapper objectMapper;

    // 즉시 전송
    // 이벤트 하나를 락 걸고 조회 -> 전송
    @Transactional
    public void sendById(Long outboxEventId) {

        outboxEventRepository.findPendingByIdForUpdate(outboxEventId)
                .ifPresent(this::send);
    }

    // 재전송
    @Transactional
    public void retryPending() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(RETRY_DELAY_SECONDS);

        outboxEventRepository.findPendingForRetry(before, RETRY_BATCH_SIZE)
                .forEach(this::send);
    }

    private void send(OutboxEvent event) {

        // JSON -> 객체로 되돌려서 전송
        try {
            OrderDataPayload payload = objectMapper.readValue(event.getPayload(), OrderDataPayload.class);
            dataPlatformClient.send(payload);
            event.markSent(); // 성공 -> SENT
        } catch (Exception e) {
            // 예외를 다시 던지면 트랜잭션이 롤백되어 재시도 카운트 증가까지 취소된다.
            // 실패도 기록을 남겨야 하므로 여기서 잡는다.
            event.recordFailure(MAX_RETRY);
            log.warn("[Outbox] 전송 실패 id={}, retryCount={}", event.getId(), event.getRetryCount(), e);
        }
    }
}