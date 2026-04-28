package com.gmail.scanner.web;

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

  private final GoogleServiceProvider googleServiceProvider;
  private final OAuth2AuthorizedClientProvider oauth2AuthorizedClientProvider;
  private final OrderService orderService;
  private final ScanResultMapper mapper;

  public Endpoint(GoogleServiceProvider googleServiceProvider,
      OAuth2AuthorizedClientProvider oauth2AuthorizedClientProvider,
      OrderService orderService,
      ScanResultMapper mapper) {
    this.googleServiceProvider = googleServiceProvider;
    this.oauth2AuthorizedClientProvider = oauth2AuthorizedClientProvider;
    this.orderService = orderService;
    this.mapper = mapper;
  }

  @GetMapping("/api/me")
  public ResponseEntity<?> me(OAuth2AuthenticationToken auth) {
    if (auth == null) {
      return ResponseEntity.status(401).build();
    }
    return ResponseEntity.ok(Map.of("email", auth.getPrincipal().getAttribute("email")));
  }

  @GetMapping("/scan/{group}/{year}")
  public ScanResult scan(@PathVariable String group, @PathVariable int year) {
    if (year < 2020 || year > LocalDate.now().getYear()) {
      throw new IllegalArgumentException("Invalid year: " + year);
    }

    Group groupEnum = Group.from(group);
    Gmail gmail = googleServiceProvider.getGmailService(oauth2AuthorizedClientProvider.getClient());
    Map<Source, List<Order>> orders = orderService.getOrderMap(gmail, year, groupEnum.getQueries());
    return this.mapper.fromOrderMap(groupEnum, year, orders);
  }
}
