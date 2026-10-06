package com.example.ch6spring.domain.outbox.client;

import com.example.ch6spring.common.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "data-platform.client", havingValue = "kafka")

public class KafkaDataPlatformClient implements DataPlatformClient {

    private static final long SEND_TIMEOUT_SECONDS = 5;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void send(OrderDataPayload payload) {

        String message = objectMapper.writeValueAsString(payload);
        String key = String.valueOf(payload.orderId());

        try {
            kafkaTemplate.send(KafkaTopicConfig.ORDER_EVENTS, key, message)
                    .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            log.info("[Kafka] 주문 이벤트 발행 order = {}", payload.orderId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Kafka 전송 중단 orderId = " + payload.orderId(), e);

        } catch (Exception e) {
            throw new IllegalStateException("Kafka 전송 실패 orderId = " + payload.orderId(), e);
        }
    }
}