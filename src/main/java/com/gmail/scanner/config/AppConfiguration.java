package com.gmail.scanner.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppConfiguration(
    String name,
    int startYear,
    GmailConfig gmail
) {

  public record GmailConfig(
      int batchSize,
      int pageMaxResults  // 500 is the Gmail API maximum
  ) {}
}
