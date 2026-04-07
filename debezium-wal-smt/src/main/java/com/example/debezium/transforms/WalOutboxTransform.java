package com.example.debezium.transforms;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.transforms.Transformation;

import java.util.Map;

public class WalOutboxTransform<R extends ConnectRecord<R>> implements Transformation<R> {
    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    private static final Schema PAYLOAD_SCHEMA = SchemaBuilder.struct()
            .name("com.example.broker.TradeEvent")
            .field("id", Schema.STRING_SCHEMA)
            .field("ticker", Schema.STRING_SCHEMA)
            .field("accountId", Schema.STRING_SCHEMA)
            .field("volume", Schema.FLOAT64_SCHEMA)
            .field("price", Schema.FLOAT64_SCHEMA)
            .field("status", Schema.STRING_SCHEMA)
            .build();

    @Override
    public R apply(R record) {
        if (record.value() == null) return record;

        try {
            Struct valueStruct = (Struct) record.value();
            Struct messageStruct = valueStruct.getStruct("message");
            if (messageStruct == null) return record;

            byte[] contentBytes = messageStruct.getBytes("content");

            String traceId = null;
            String aggregateId = null;
            String eventId = null;

            String ticker = null;
            String accountId = null;
            double volume = 0.0;
            double price = 0.0;
            String status = null;

            try (JsonParser parser = JSON_FACTORY.createParser(contentBytes)) {
                while (parser.nextToken() != JsonToken.END_OBJECT && parser.currentToken() != null) {
                    String fieldName = parser.currentName();

                    if ("traceId".equals(fieldName)) {
                        parser.nextToken();
                        traceId = parser.getText();
                    } else if ("aggregateId".equals(fieldName)) {
                        parser.nextToken();
                        aggregateId = parser.getText();
                    } else if ("id".equals(fieldName)) {
                        parser.nextToken();
                        eventId = parser.getText();
                    } else if ("payload".equals(fieldName)) {
                        parser.nextToken();
                        while (parser.nextToken() != JsonToken.END_OBJECT) {
                            String innerField = parser.currentName();
                            parser.nextToken();

                            if ("id".equals(innerField) && eventId == null) {
                                eventId = parser.getText();
                            } else if ("ticker".equals(innerField)) {
                                ticker = parser.getText();
                            } else if ("accountId".equals(innerField)) {
                                accountId = parser.getText();
                            } else if ("volume".equals(innerField)) {
                                volume = parser.getDoubleValue();
                            } else if ("price".equals(innerField)) {
                                price = parser.getDoubleValue();
                            } else if ("status".equals(innerField)) {
                                status = parser.getText();
                            }
                        }
                    }
                }
            }

            if (traceId != null && !traceId.equals("no-trace")) {
                record.headers().addString("traceparent", traceId);
            }

            Struct payloadStruct = new Struct(PAYLOAD_SCHEMA)
                    .put("id", eventId != null ? eventId : aggregateId)
                    .put("ticker", ticker)
                    .put("accountId", accountId)
                    .put("volume", volume)
                    .put("price", price)
                    .put("status", status);

            return record.newRecord(
                    record.topic(),
                    record.kafkaPartition(),
                    Schema.STRING_SCHEMA,
                    aggregateId,
                    PAYLOAD_SCHEMA,
                    payloadStruct,
                    record.timestamp()
            );
        } catch (Exception e) {
            return record;
        }
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef();
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> configs) {
    }
}