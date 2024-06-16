package com.example.currencyApplication.command;

import com.example.currencyApplication.service.CurrencyService;
import org.springframework.http.ResponseEntity;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryCommand extends Command {
    private String consumer;
    private String currency;
    private Integer period;

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

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    @Override
    public ResponseEntity<?> handle(CurrencyService currencyService) {
        Map<String, Object> request = new HashMap<>();
        request.put("requestId", getId());
        request.put("client", getConsumer());
        request.put("currency", getCurrency());
        request.put("period", getPeriod());

        List<Map<String, Object>> history = currencyService.handleCurrencyHistoryRequest(request);
        return ResponseEntity.ok(history);
    }
}