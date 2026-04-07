package com.example.brokercore.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class OutboxMessageDto {
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String aggregateType;
    private String aggregateId;
    private String eventType;
    private Object payload;
    private String traceId;
    @Builder.Default
    private Instant createdAt = Instant.now();
}