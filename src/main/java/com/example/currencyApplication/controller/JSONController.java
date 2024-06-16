package com.example.currencyApplication.controller;

import com.example.currencyApplication.service.CurrencyService;
import com.example.currencyApplication.service.MessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/json_api")
public class JSONController {

    private static final Logger logger = LoggerFactory.getLogger(JSONController.class);

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private MessagePublisher messagePublisher;

    @PostMapping("/current")
    public ResponseEntity<?> getCurrentCurrency(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> response = currencyService.handleCurrentCurrencyRequest(request);
            messagePublisher.sendMessage(request.toString());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Duplicate request detected: {}", request, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error processing current currency request: {}", request, e);
            return ResponseEntity.badRequest().body("Unexpected error occurred");
        }
    }

    @PostMapping("/history")
    public ResponseEntity<?> getCurrencyHistory(@RequestBody Map<String, Object> request) {
        try {
            List<Map<String, Object>> response = currencyService.handleCurrencyHistoryRequest(request);
            messagePublisher.sendMessage(request.toString());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Duplicate request detected: {}", request, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) { // SHOULD be catched specifically not like this general "Exception" 
            logger.error("Unexpected error processing currency history request: {}", request, e);
            return ResponseEntity.badRequest().body("Unexpected error occurred");
        }
    }
}