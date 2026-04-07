package com.example.brokerrisk.repository;

import com.example.brokerrisk.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {

    void deleteByProcessedAtBefore(Instant time);

    @Modifying
    @Query(value = "INSERT INTO idempotent_consumer_log (event_id, processed_at) VALUES (:eventId, :processedAt) ON CONFLICT (event_id) DO NOTHING", nativeQuery = true)
    int insertIfNotExists(@Param("eventId") String eventId, @Param("processedAt") Instant processedAt);
}