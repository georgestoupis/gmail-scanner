package com.gmail.scanner.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ScanResult(Group group,
                         String period,
                         List<SourceResult> sources,
                         BigDecimal totalSum,
                         BigDecimal avgMonth,
                         Map<Integer, BigDecimal> periodTotals,
                         String msg) {

}
