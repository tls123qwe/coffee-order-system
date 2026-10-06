package com.example.ch6spring.domain.outbox.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "data-platform.client", havingValue = "mock", matchIfMissing = true)

// 가짜 수집 플랫폼
// 로그만 남기는 중
public class MockDataPlatformClient implements DataPlatformClient {

    @Override
    public void send(OrderDataPayload payload) {

        log.info("[DataPlatform] 주문 데이타 전송 : {}", payload);
    }
}