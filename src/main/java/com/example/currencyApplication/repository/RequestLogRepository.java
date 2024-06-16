package com.example.currencyApplication.repository;

import com.example.currencyApplication.model.entity.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestLogRepository  extends JpaRepository<RequestLog, String> {
}
