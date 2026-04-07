package com.example.brokercore.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TradeRequest {
    private String ticker;
    private String accountId;
    private BigDecimal volume;
    private BigDecimal price;
}