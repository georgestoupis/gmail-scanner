package com.gmail.scanner.model;

import com.gmail.scanner.service.model.Source;
import java.math.BigDecimal;

public record SourceResult(Source source, int orders, BigDecimal sum) {

}
