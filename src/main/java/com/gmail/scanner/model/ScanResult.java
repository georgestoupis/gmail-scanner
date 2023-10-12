package com.gmail.scanner.model;

import java.util.List;

public record ScanResult(String group, String period, List<SourceResult> sources, double totalSum, String msg) {

}
