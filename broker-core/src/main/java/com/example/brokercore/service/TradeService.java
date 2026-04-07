package com.example.brokercore.service;

import com.example.brokercore.dto.OutboxMessageDto;
import com.example.brokercore.dto.TradeRequest;
import com.example.brokercore.entity.Trade;
import com.example.brokercore.entity.TradeStatus;
import com.example.brokercore.repository.TradeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService {
    private final TradeRepository tradeRepository;

    private final ObjectMapper objectMapper;
    private final ObjectProvider<Tracer> tracerProvider;

    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    public Trade executeTrade(TradeRequest request) {
        log.info("Processing trade execution for account: {}, ticker: {}", request.getAccountId(), request.getTicker());

        var trade = Trade.builder()
                .ticker(request.getTicker())
                .accountId(request.getAccountId())
                .volume(request.getVolume())
                .price(request.getPrice())
                .status(TradeStatus.EXECUTED)
                .build();

        var tracer = tracerProvider.getIfAvailable();
        String w3cTraceparent = "no-trace";
        if (tracer != null && tracer.currentSpan() != null) {
            var context = tracer.currentSpan().context();
            String traceFlags = Boolean.TRUE.equals(context.sampled()) ? "01" : "00";
            w3cTraceparent = String.format("00-%s-%s-%s", context.traceId(), context.spanId(), traceFlags);
        }

        final String finalTraceparent = w3cTraceparent;

        return transactionTemplate.execute(status -> {
            try {
                var savedTrade = tradeRepository.saveAndFlush(trade);

                var event = OutboxMessageDto.builder()
                        .aggregateType("Trade")
                        .aggregateId(savedTrade.getAccountId())
                        .eventType("TradeCreated")
                        .payload(savedTrade)
                        .traceId(finalTraceparent)
                        .build();

                String eventJson = objectMapper.writeValueAsString(event);

                jdbcTemplate.queryForObject(
                        "SELECT pg_logical_emit_message(true, 'outbox', ?::text)",
                        String.class,
                        eventJson
                );

                log.info("Successfully executed trade and emitted message directly to WAL. Trade ID: {}", savedTrade.getId());
                return savedTrade;

            } catch (Exception e) {
                log.error("Failed to execute DB operations", e);
                status.setRollbackOnly();
                throw new RuntimeException("Transaction failed", e);
            }
        });
    }
}