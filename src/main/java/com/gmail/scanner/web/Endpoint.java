package com.gmail.scanner.web;

import com.gmail.scanner.google.GoogleServiceProvider;
import com.gmail.scanner.mapper.ScanResultMapper;
import com.gmail.scanner.model.ScanResult;
import com.gmail.scanner.security.OAuth2AuthorizedClientProvider;
import com.gmail.scanner.service.FoodService;
import com.gmail.scanner.service.GamesService;
import com.gmail.scanner.service.model.Order;
import com.gmail.scanner.service.parser.HtmlParser;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
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
  private final HtmlParser htmlParser;
  private final ScanResultMapper mapper;

  public Endpoint(GoogleServiceProvider googleServiceProvider, OAuth2AuthorizedClientProvider oauth2AuthorizedClientProvider, HtmlParser htmlParser,
      ScanResultMapper mapper) {
    this.googleServiceProvider = googleServiceProvider;
    this.oauth2AuthorizedClientProvider = oauth2AuthorizedClientProvider;
    this.htmlParser = htmlParser;
    this.mapper = mapper;
  }

  @GetMapping("/scan/food/{year}")
  public ScanResult food(@PathVariable int year) throws IOException, GeneralSecurityException {
    FoodService foodService = new FoodService(googleServiceProvider, oauth2AuthorizedClientProvider, htmlParser);
    List<Order> foodOrders = foodService.getAllOrders(year);
    return this.mapper.fromOrderList("food", year, foodOrders);
  }

  @GetMapping("/scan/games/{year}")
  public ScanResult games(@PathVariable int year) throws IOException, GeneralSecurityException {
    GamesService gamesService = new GamesService(googleServiceProvider, oauth2AuthorizedClientProvider, htmlParser);
    List<Order> orders = gamesService.getAllOrders(year);
    return this.mapper.fromOrderList("games", year, orders);
  }

}