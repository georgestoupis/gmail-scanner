package com.gmail.scanner.model;

import java.math.BigDecimal;
import java.util.List;

public record ScanResult(String group,
                         String period,
                         List<SourceResult> sources,
                         BigDecimal totalSum,
                         BigDecimal avgMonth,
                         String msg) {

}
