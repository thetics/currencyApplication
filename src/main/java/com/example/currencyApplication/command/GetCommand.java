package com.example.currencyApplication.command;

import com.example.currencyApplication.service.CurrencyService;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class GetCommand extends Command {
    private String consumer;
    private String currency;

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public ResponseEntity<?> handle(CurrencyService currencyService) {
        Map<String, Object> request = new HashMap<>();
        request.put("requestId", getId());
        request.put("client", getConsumer());
        request.put("currency", getCurrency());

        Map<String, Object> currencyData = currencyService.handleCurrentCurrencyRequest(request);
        return ResponseEntity.ok(currencyData);
    }
}