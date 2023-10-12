package com.gmail.scanner.model;

import com.gmail.scanner.service.model.Source;

public record SourceResult(Source source, int orders, double sum) {

}
