package com.gmail.scanner.web;

import com.gmail.scanner.exception.UnsupportedGroupException;
import com.gmail.scanner.google.GoogleServiceProvider;
import com.gmail.scanner.mapper.ScanResultMapper;
import com.gmail.scanner.model.ScanResult;
import com.gmail.scanner.security.OAuth2AuthorizedClientProvider;
import com.gmail.scanner.service.OrderService;
import com.gmail.scanner.service.model.Order;
import com.gmail.scanner.service.model.Source;
import com.gmail.scanner.service.queries.FoodQueries;
import com.gmail.scanner.service.queries.GameQueries;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Endpoint {

  private static final Logger LOG = LoggerFactory.getLogger(Endpoint.class);

  private final GoogleServiceProvider googleServiceProvider;
  private final OAuth2AuthorizedClientProvider oauth2AuthorizedClientProvider;
  private final ScanResultMapper mapper;

  public Endpoint(GoogleServiceProvider googleServiceProvider,
      OAuth2AuthorizedClientProvider oauth2AuthorizedClientProvider,
      ScanResultMapper mapper) {
    this.googleServiceProvider = googleServiceProvider;
    this.oauth2AuthorizedClientProvider = oauth2AuthorizedClientProvider;
    this.mapper = mapper;
  }

  @GetMapping("/scan/{group}/{year}")
  public ScanResult scan(@PathVariable String group, @PathVariable int year) throws IOException, GeneralSecurityException {

    Map<Source, String> queries = switch (group) {
      case "food" -> FoodQueries.SOURCE_QUERIES_MAP;
      case "games" -> GameQueries.SOURCE_QUERIES_MAP;
      default -> throw new UnsupportedGroupException("Unsupported group: " + group);
    };

    OrderService orderService = new OrderService(googleServiceProvider, oauth2AuthorizedClientProvider);
    Map<Source, List<Order>> orders = orderService.getOrderMap(year, queries);
    return this.mapper.fromOrderMap(group, year, orders);
  }
}