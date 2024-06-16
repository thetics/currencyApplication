package com.example.currencyApplication.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


import java.io.Serializable;
import java.time.LocalDateTime;


@Entity
@Table(name = "request_log")
public class RequestLog implements Serializable {

    @Id
    @Column(name = "request_id")
    private String requestId;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "currency")
    private String currency;

    @Column(name = "period")
    private Integer period;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}