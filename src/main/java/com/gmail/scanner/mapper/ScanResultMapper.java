package com.gmail.scanner.mapper;

import com.gmail.scanner.model.ScanResult;
import com.gmail.scanner.model.SourceResult;
import com.gmail.scanner.service.model.Order;
import com.gmail.scanner.service.model.Source;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ScanResultMapper {

  public ScanResult fromOrderMap(String type, int year, Map<Source, List<Order>> orders) {
    List<SourceResult> sourceResultList = new ArrayList<>();
    orders.forEach(
        (source, list) -> sourceResultList.add(new SourceResult(source, list.size(), list.stream().mapToDouble(o -> Double.parseDouble(o.getPrice())).sum())));
    return new ScanResult(type, String.valueOf(year), sourceResultList, sourceResultList.stream().mapToDouble(SourceResult::sum).sum(), "Shame on you.");
  }

}
