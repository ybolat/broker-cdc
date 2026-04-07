package com.example.brokerrisk.scheduler;

import com.example.brokerrisk.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyCleanupJob {
    private final ProcessedEventRepository processedEventRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanOldIdempotencyKeys() {
        Instant cutoffTime = Instant.now().minus(7, ChronoUnit.DAYS);

        log.info("Starting cleanup of old idempotency keys older than: {}", cutoffTime);

        try {
            processedEventRepository.deleteByProcessedAtBefore(cutoffTime);
            log.info("Cleanup of idempotent_consumer_log table completed successfully.");
        } catch (Exception e) {
            log.error("Failed to clean up old idempotency keys", e);
        }
    }
}