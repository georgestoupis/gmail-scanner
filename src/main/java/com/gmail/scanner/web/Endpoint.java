package com.gmail.scanner.web;

import com.gmail.scanner.google.GoogleServiceProvider;
import com.gmail.scanner.mapper.ScanResultMapper;
import com.gmail.scanner.model.ScanResult;
import com.gmail.scanner.security.OAuth2AuthorizedClientProvider;
import com.gmail.scanner.service.OrderService;
import com.gmail.scanner.service.model.Order;
import com.gmail.scanner.service.model.Source;
import com.gmail.scanner.service.parser.EmailParser;
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
  private final EmailParser emailParser;
  private final ScanResultMapper mapper;


  public Endpoint(GoogleServiceProvider googleServiceProvider, OAuth2AuthorizedClientProvider oauth2AuthorizedClientProvider, EmailParser emailParser,
      ScanResultMapper mapper) throws GeneralSecurityException, IOException {
    this.googleServiceProvider = googleServiceProvider;
    this.oauth2AuthorizedClientProvider = oauth2AuthorizedClientProvider;
    this.emailParser = emailParser;
    this.mapper = mapper;
  }

  @GetMapping("/scan/food/{year}")
  public ScanResult food(@PathVariable int year) throws IOException, GeneralSecurityException {
    OrderService orderService = new OrderService(googleServiceProvider, oauth2AuthorizedClientProvider, emailParser);
    Map<Source, List<Order>> foodOrders = orderService.getOrderMap(year, FoodQueries.SOURCE_QUERIES_MAP);
    return this.mapper.fromOrderMap("food", year, foodOrders);
  }

  @GetMapping("/scan/games/{year}")
  public ScanResult games(@PathVariable int year) throws IOException, GeneralSecurityException {
    OrderService orderService = new OrderService(googleServiceProvider, oauth2AuthorizedClientProvider, emailParser);
    Map<Source, List<Order>> orders = orderService.getOrderMap(year, GameQueries.SOURCE_QUERIES_MAP);
    return this.mapper.fromOrderMap("games", year, orders);
  }

}