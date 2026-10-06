package com.example.ch6spring.domain.outbox.repository;

import com.example.ch6spring.domain.outbox.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * 즉시 전송용: 특정 이벤트 하나를 락을 걸고 조회한다.
     * - FOR UPDATE : 처리하는 동안 다른 곳에서 못 건드리게 락
     * - SKIP LOCKED : 이미 누가 락을 잡고 있으면 기다리지 않고 건너뜀 (중복 전송 방지)
     * - status = 'PENDING' : 이미 보낸 이벤트는 다시 보내지 않음
     */
    @Query(value = """
            SELECT * FROM outbox_event
            WHERE id = :id AND status = 'PENDING'
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    Optional<OutboxEvent> findPendingByIdForUpdate(@Param("id") Long id);

    /**
     * 재전송용: 아직 못 보낸 이벤트들을 오래된 순으로 가져온다.
     * - created_at < :before : 방금 생긴 이벤트는 즉시 전송이 처리 중일 수 있으니 제외
     * - LIMIT :limit : 한 번에 너무 많이 가져오지 않도록 제한
     * - SKIP LOCKED : 서버 여러 대의 스케줄러가 같은 이벤트를 집지 않도록 함
     */
    @Query(value = """
            SELECT * FROM outbox_event
            WHERE status = 'PENDING' AND created_at < :before
            ORDER BY id
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> findPendingForRetry(@Param("before") LocalDateTime before,
                                          @Param("limit") int limit);
}