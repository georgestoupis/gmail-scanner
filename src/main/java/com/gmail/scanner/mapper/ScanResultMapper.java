package com.gmail.scanner.mapper;

import com.gmail.scanner.model.ScanResult;
import com.gmail.scanner.service.model.Order;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ScanResultMapper {

  public ScanResult fromOrderList(String type, int year, List<Order> orders) {
    double sum = orders.stream().mapToDouble(o -> Double.parseDouble(o.getPrice())).sum();
    return new ScanResult(type, String.valueOf(year), orders.size(), sum, sum / 12, "OK");
  }

}
