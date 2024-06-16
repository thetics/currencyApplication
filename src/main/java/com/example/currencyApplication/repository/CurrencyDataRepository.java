package com.example.currencyApplication.repository;

import com.example.currencyApplication.model.entity.CurrencyData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CurrencyDataRepository extends JpaRepository<CurrencyData,Long> {
    CurrencyData findFirstByCurrencyOrderByTimestampDesc(String currency);
    List<CurrencyData> findByCurrencyAndTimestampAfter(String currency, LocalDateTime timestamp);
}
