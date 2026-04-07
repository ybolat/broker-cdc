package com.example.brokercore.controller;

import com.example.brokercore.dto.TradeRequest;
import com.example.brokercore.entity.Trade;
import com.example.brokercore.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @PostMapping
    public ResponseEntity<Trade> createTrade(@RequestBody TradeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tradeService.executeTrade(request));
    }
}