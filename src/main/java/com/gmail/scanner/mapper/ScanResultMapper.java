package com.gmail.scanner.mapper;

import com.gmail.scanner.model.Group;
import com.gmail.scanner.model.ScanResult;
import com.gmail.scanner.model.SourceResult;
import com.gmail.scanner.service.model.Order;
import com.gmail.scanner.service.model.Source;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ScanResultMapper {

  public ScanResult fromOrderMap(Group group, int year, Map<Source, List<Order>> orders) {

    List<SourceResult> sourceResultList = orders.entrySet()
        .stream()
        .map(entry -> toSourceResult(entry.getKey(), entry.getValue()))
        .toList();

    BigDecimal totalSum = sourceResultList.stream()
        .map(SourceResult::sum)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    int monthsToDate = year == LocalDate.now().getYear() ? LocalDate.now().getMonthValue() : 12;

    BigDecimal avgPerMonth = totalSum.divide(BigDecimal.valueOf(monthsToDate), RoundingMode.HALF_UP);

    Map<Integer, BigDecimal> monthlyTotals = orders.values().stream()
        .flatMap(List::stream)
        .collect(Collectors.groupingBy(o -> o.date().getMonthValue(),
            Collectors.reducing(BigDecimal.ZERO, this::price, BigDecimal::add)
        ));

    return new ScanResult(group, String.valueOf(year), sourceResultList, totalSum, avgPerMonth, monthlyTotals, group.message);
  }

  private SourceResult toSourceResult(Source source, List<Order> orders) {
    BigDecimal sumOfOrders = orders.stream()
        .map(o -> new BigDecimal(o.price()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    return new SourceResult(source, orders.size(), sumOfOrders);
  }

  private BigDecimal price(Order order) {
    return new BigDecimal(order.price());
  }
}
