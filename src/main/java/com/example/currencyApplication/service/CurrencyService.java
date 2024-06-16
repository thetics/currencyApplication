package com.example.currencyApplication.service;

import com.example.currencyApplication.config.FixerConfig;
import com.example.currencyApplication.model.entity.CurrencyData;
import com.example.currencyApplication.model.entity.RequestLog;
import com.example.currencyApplication.repository.CurrencyDataRepository;
import com.example.currencyApplication.repository.RequestLogRepository;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CurrencyService {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);

    private final FixerConfig fixerConfig;
    private final RequestLogRepository requestLogRepository;
    private final CurrencyDataRepository currencyDataRepository;
    private final AmqpTemplate amqpTemplate;
    private final RestTemplate restTemplate;

    @Autowired
    public CurrencyService(FixerConfig fixerConfig,
                           RequestLogRepository requestLogRepository,
                           CurrencyDataRepository currencyDataRepository,
                           AmqpTemplate amqpTemplate,
                           RestTemplate restTemplate) {
        this.fixerConfig = fixerConfig;
        this.requestLogRepository = requestLogRepository;
        this.currencyDataRepository = currencyDataRepository;
        this.amqpTemplate = amqpTemplate;
        this.restTemplate = restTemplate;
    }

    public Optional<RequestLog> findRequestLogById(String requestId) {
        return requestLogRepository.findById(requestId);
    }

    public boolean isDuplicateRequest(String requestId) {
        return requestLogRepository.existsById(requestId);
    }

    public Map<String, Object> handleCurrentCurrencyRequest(Map<String, Object> request) {
        String requestId = (String) request.get("requestId");
        Long timestamp = (Long) request.get("timestamp");
        String client = (String) request.get("client");
        String currency = (String) request.get("currency");

        if (isDuplicateRequest(requestId)) {
            logger.warn("Duplicate request ID: {}", requestId);
            throw new IllegalArgumentException("Duplicate request ID: " + requestId);
        }

        try {
            fetchAndSaveCurrencyData(currency);
        } catch (RestClientException e) {
            logger.error("Failed to fetch currency data: {}", e.getMessage(), e);
            throw new IllegalStateException("Error fetching currency data", e);
        }

        CurrencyData currencyData = currencyDataRepository.findFirstByCurrencyOrderByTimestampDesc(currency);
        logRequest(requestId, timestamp, client, currency, "EXT_SERVICE_1", null);

        return convertCurrencyDataToMap(currencyData);
    }

    public List<Map<String, Object>> handleCurrencyHistoryRequest(Map<String, Object> request) {
        String requestId = (String) request.get("requestId");
        Long timestamp = (Long) request.get("timestamp");
        String client = (String) request.get("client");
        String currency = (String) request.get("currency");
        Integer period = (Integer) request.get("period");

        if (isDuplicateRequest(requestId)) {
            logger.warn("Duplicate request ID: {}", requestId);
            throw new IllegalArgumentException("Duplicate request ID: " + requestId);
        }

        try {
            fetchAndSaveHistoricalCurrencyData(currency, period);
        } catch (RestClientException e) {
            logger.error("Failed to fetch historical currency data: {}", e.getMessage(), e);
            throw new IllegalStateException("Error fetching historical currency data", e);
        }

        LocalDateTime startTime = LocalDateTime.now().minusHours(period);
        List<CurrencyData> history = currencyDataRepository.findByCurrencyAndTimestampAfter(currency, startTime);
        logRequest(requestId, timestamp, client, currency, "EXT_SERVICE_1", period);

        return history.stream().map(this::convertCurrencyDataToMap).collect(Collectors.toList());
    }

    private void fetchAndSaveCurrencyData(String currency) {
        String url = fixerConfig.getApiUrl() + "/latest?access_key=" + fixerConfig.getAccessKey() + "&symbols=" + currency;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response != null && (boolean) response.get("success")) {
            saveCurrencyData(response, currency);
        } else {
            throw new RestClientException("Failed to fetch latest currency data");
        }
    }

    private void saveCurrencyData(Map<String, Object> response, String currency) {
        Map<String, Double> rates = (Map<String, Double>) response.get("rates");
        Double rate = Double.parseDouble(String.valueOf(rates.get(currency)));
        CurrencyData currencyData = new CurrencyData();
        currencyData.setCurrency(currency);
        currencyData.setRate(rate);
        currencyData.setTimestamp(LocalDateTime.now());
        currencyDataRepository.save(currencyData);
    }

    private void fetchAndSaveHistoricalCurrencyData(String currency, Integer period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusHours(period);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startDateString = startDate.format(formatter);
        String endDateString = now.format(formatter);

        String url = fixerConfig.getApiUrl() + "/timeseries?access_key=" + fixerConfig.getAccessKey() + "&start_date=" + startDateString + "&end_date=" + endDateString + "&symbols=" + currency;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response != null && (boolean) response.get("success")) {
            saveHistoricalCurrencyData(response, currency, formatter);
        } else {
            throw new RestClientException("Failed to fetch historical currency data");
        }
    }

    private void saveHistoricalCurrencyData(Map<String, Object> response, String currency, DateTimeFormatter formatter) {
        Map<String, Map<String, Double>> rates = (Map<String, Map<String, Double>>) response.get("rates");
        for (Map.Entry<String, Map<String, Double>> entry : rates.entrySet()) {
            String date = entry.getKey();
            LocalDateTime timestamp = LocalDateTime.parse(date, formatter);
            Double rate = entry.getValue().get(currency);
            CurrencyData currencyData = new CurrencyData();
            currencyData.setCurrency(currency);
            currencyData.setRate(rate);
            currencyData.setTimestamp(timestamp);
            currencyDataRepository.save(currencyData);
        }
    }

    private void logRequest(String requestId, Long timestamp, String client, String currency, String serviceName, Integer period) {
        RequestLog log = new RequestLog();
        log.setRequestId(requestId);
        log.setServiceName(serviceName);
        log.setTimestamp(timestamp != null ? LocalDateTime.ofEpochSecond(timestamp / 1000, 0, ZoneOffset.UTC) : LocalDateTime.now());
        log.setClientId(client);
        log.setCurrency(currency);
        log.setPeriod(period);
        requestLogRepository.save(log);
        amqpTemplate.convertAndSend("request_statistics", "", log);
    }

    private Map<String, Object> convertCurrencyDataToMap(CurrencyData currencyData) {
        Map<String, Object> map = new HashMap<>();
        map.put("currency", currencyData.getCurrency());
        map.put("rate", currencyData.getRate());
        map.put("timestamp", currencyData.getTimestamp().toString());
        return map;
    }

    // Scheduled method to update currency data periodically
    @Scheduled(fixedRateString = "${currency.update.interval}")
    public void updateCurrencyData() {
        List<String> currencies = List.of("USD", "EUR", "GBP");  // Add more currencies as needed
        for (String currency : currencies) {
            try {
                fetchAndSaveCurrencyData(currency);
            } catch (RestClientException e) {
                logger.error("Failed to update currency data for {}: {}", currency, e.getMessage(), e);
            }
        }
    }
}