package com.gmail.scanner.mapper;

import com.gmail.scanner.model.ScanResult;
import com.gmail.scanner.model.SourceResult;
import com.gmail.scanner.service.model.Order;
import com.gmail.scanner.service.model.Source;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ScanResultMapper {

  public ScanResult fromOrderMap(String group, int year, Map<Source, List<Order>> orders) {

    List<SourceResult> sourceResultList = orders.entrySet()
        .stream()
        .map(entry -> createSourceResultFromArgs(entry.getKey(), entry.getValue()))
        .toList();

    BigDecimal totalSum = sourceResultList.stream()
        .map(SourceResult::sum)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    int monthsToDate = year == LocalDate.now().getYear() ? LocalDate.now().getMonthValue() : 12;

    BigDecimal avgPerMonth = totalSum.divide(BigDecimal.valueOf(monthsToDate), RoundingMode.HALF_UP);

    return new ScanResult(group, String.valueOf(year), sourceResultList, totalSum, avgPerMonth, "Shame on you.");
  }

  private SourceResult createSourceResultFromArgs(Source source, List<Order> orders) {

    BigDecimal sumOfOrders = orders.stream()
        .map(o -> new BigDecimal(o.getPrice()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return new SourceResult(source, orders.size(), sumOfOrders);
  }
}
