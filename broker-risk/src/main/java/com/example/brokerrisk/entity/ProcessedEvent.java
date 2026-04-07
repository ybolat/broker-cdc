package com.example.brokerrisk.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "idempotent_consumer_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEvent {
    @Id
    private String eventId;
    private Instant processedAt;

    public ProcessedEvent(String eventId) {
        this.eventId = eventId;
        this.processedAt = Instant.now();
    }
}