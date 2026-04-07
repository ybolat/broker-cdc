package com.example.brokerrisk.consumer;

import com.example.brokerrisk.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeEventConsumer {
    private final ProcessedEventRepository processedEventRepository;

    @KafkaListener(topics = "broker.events.TradeAvro", groupId = "risk-management-group")
    @Transactional
    public void consumeTradeEvent(ConsumerRecord<Object, Object> record) {
        GenericRecord tradeData = (GenericRecord) record.value();

        String eventId = tradeData.get("id").toString();
        String ticker = tradeData.get("ticker").toString();
        double volume = (Double) tradeData.get("volume");

        int insertedRows = processedEventRepository.insertIfNotExists(eventId, Instant.now());
        if (insertedRows == 0) {
            log.warn("Duplicate. Skipping: {}", eventId);
            return;
        }

        if (volume < 0) {
            throw new IllegalArgumentException("Negative volume: " + volume);
        }

        log.info("The transaction {} under ticker {} has been approved.", eventId, ticker);
    }
}