package com.example.ch6spring.dataplatform;

import com.example.ch6spring.common.config.KafkaTopicConfig;
import com.example.ch6spring.domain.outbox.client.OrderDataPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor

// 외부 데이터 수집 플랫폼을 흉내 내는 컨슈머
// 외부라는 의미로 패키지 분리
public class DataPlatformConsumer {

    private static final Duration DEDUP_TTL = Duration.ofDays(7);
    private static final String DEDUP_KEY_PREFIX = "data-platform:processed:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopicConfig.ORDER_EVENTS, groupId = "data-platform")
    public void consume(String message) {

        OrderDataPayload payload = objectMapper.readValue(message, OrderDataPayload.class);
        String key = DEDUP_KEY_PREFIX + payload.orderId();

        Boolean first = redisTemplate.opsForValue().setIfAbsent(key, "1", DEDUP_TTL);

        if (!Boolean.TRUE.equals(first)) {
            log.info("[DataPlatform] 중복 메시지 무시 orderId = {}", payload.orderId());
            return;
        }

        log.info("[DataPlatform] 주문 데이터 수신 {}", payload);
    }
}