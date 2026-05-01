package com.gmail.scanner.web;

import com.gmail.scanner.config.AppConfiguration;
import com.gmail.scanner.google.GoogleServiceProvider;
import com.gmail.scanner.mapper.ScanResultMapper;
import com.gmail.scanner.model.Group;
import com.gmail.scanner.model.ScanResult;
import com.gmail.scanner.security.OAuth2AuthorizedClientProvider;
import com.gmail.scanner.service.OrderService;
import com.gmail.scanner.service.model.Order;
import com.gmail.scanner.service.model.Source;
import com.google.api.services.gmail.Gmail;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Endpoint {

  private final AppConfiguration appConfig;
  private final GoogleServiceProvider googleServiceProvider;
  private final OAuth2AuthorizedClientProvider oauth2AuthorizedClientProvider;
  private final OrderService orderService;
  private final ScanResultMapper mapper;

  public Endpoint(AppConfiguration appConfig,
      GoogleServiceProvider googleServiceProvider,
      OAuth2AuthorizedClientProvider oauth2AuthorizedClientProvider,
      OrderService orderService,
      ScanResultMapper mapper) {
    this.appConfig = appConfig;
    this.googleServiceProvider = googleServiceProvider;
    this.oauth2AuthorizedClientProvider = oauth2AuthorizedClientProvider;
    this.orderService = orderService;
    this.mapper = mapper;
  }

  @GetMapping("/api/config")
  public Map<String, Object> config() {
    return Map.of("name", appConfig.name(), "startYear", appConfig.startYear());
  }

  @GetMapping("/api/me")
  public ResponseEntity<?> me(OAuth2AuthenticationToken auth) {
    if (auth == null) {
      return ResponseEntity.status(401).build();
    }
    return ResponseEntity.ok(Map.of("email", auth.getPrincipal().getAttribute("email")));
  }

  @GetMapping("/scan/{group}/all")
  public ScanResult scanAllTime(@PathVariable String group) {
    Group groupEnum = Group.from(group);
    Gmail gmail = googleServiceProvider.getGmailService(oauth2AuthorizedClientProvider.getClient());
    Map<Source, List<Order>> orders = orderService.getOrderMapAllTime(gmail, groupEnum.getQueries());
    return this.mapper.fromOrderMapAllTime(groupEnum, orders);
  }

  @GetMapping("/scan/{group}/{year}")
  public ScanResult scan(@PathVariable String group, @PathVariable int year) {
    if (year < appConfig.startYear() || year > LocalDate.now().getYear()) {
      throw new IllegalArgumentException("Invalid year: " + year);
    }

    Group groupEnum = Group.from(group);
    Gmail gmail = googleServiceProvider.getGmailService(oauth2AuthorizedClientProvider.getClient());
    Map<Source, List<Order>> orders = orderService.getOrderMapForYear(gmail, groupEnum.getQueries(), year);
    return this.mapper.fromOrderMap(groupEnum, year, orders);
  }
}
