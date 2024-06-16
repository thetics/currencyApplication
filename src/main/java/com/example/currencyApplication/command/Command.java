package com.example.currencyApplication.command;

import com.example.currencyApplication.service.CurrencyService;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.http.ResponseEntity;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = GetCommand.class, name = "get"),
        @JsonSubTypes.Type(value = HistoryCommand.class, name = "history")
})
public abstract class Command {
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public abstract ResponseEntity<?> handle(CurrencyService currencyService);
}