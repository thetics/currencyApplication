package com.example.currencyApplication.service;

import com.example.currencyApplication.model.entity.RequestLog;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQPublisher {

    @Autowired
    private AmqpTemplate amqpTemplate;

    public void publishRequestLog(RequestLog log) {
        amqpTemplate.convertAndSend("request_statistics", log);
    }
}
