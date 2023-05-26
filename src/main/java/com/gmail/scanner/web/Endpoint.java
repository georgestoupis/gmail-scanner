package com.gmail.scanner.web;

import com.gmail.scanner.google.GoogleServiceProvider;
import com.gmail.scanner.model.ScanResult;
import com.gmail.scanner.security.OAuth2AuthorizedClientProvider;
import com.gmail.scanner.service.FoodService;
import com.gmail.scanner.service.model.FoodOrder;
import com.gmail.scanner.service.model.FoodOrderSource;
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

  public Endpoint(GoogleServiceProvider googleServiceProvider, OAuth2AuthorizedClientProvider oauth2AuthorizedClientProvider, HtmlParser htmlParser) {
    this.googleServiceProvider = googleServiceProvider;
    this.oauth2AuthorizedClientProvider = oauth2AuthorizedClientProvider;
    this.htmlParser = htmlParser;
  }

  @GetMapping("/")
  public String index() {
    return "Greetings from Spring Boot!";
  }

  @GetMapping("/scan/food/{year}")
  public ScanResult scan(@PathVariable int year) throws IOException, GeneralSecurityException {
    FoodService foodService = new FoodService(googleServiceProvider, oauth2AuthorizedClientProvider, htmlParser);
    List<FoodOrder> foodOrders = foodService.getOrders(year, FoodOrderSource.EFOOD);
    double sum = foodOrders.stream().mapToDouble(o -> Double.parseDouble(o.getPrice())).sum();
    ScanResult scanResult = new ScanResult("food", String.valueOf(year), foodOrders.size(), sum);
    LOG.info("Result: {}", scanResult);
    return scanResult;
  }
}