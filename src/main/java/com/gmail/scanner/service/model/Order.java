package com.gmail.scanner.service.model;

import java.time.LocalDateTime;

public record Order(Source source, LocalDateTime date, String price) {
}
