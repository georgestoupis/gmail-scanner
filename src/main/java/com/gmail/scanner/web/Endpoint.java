package com.gmail.scanner.web;

import com.gmail.scanner.google.GoogleServiceProvider;
import com.gmail.scanner.security.OAuth2AuthorizedClientProvider;
import com.gmail.scanner.service.EfoodService;
import com.gmail.scanner.service.WoltService;
import com.gmail.scanner.service.model.EfoodOrder;
import com.gmail.scanner.service.model.WoltOrder;
import com.gmail.scanner.service.parser.HtmlParser;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO:
 * 1. Add unit tests
 * 2. Decide where this will run & how frequently
 */
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

  @GetMapping("/scan")
  public String scan() throws IOException, GeneralSecurityException {

    StringBuilder builder = new StringBuilder();

    EfoodService efoodService = new EfoodService(googleServiceProvider, oauth2AuthorizedClientProvider, htmlParser);
    List<EfoodOrder> efoodOrders = efoodService.getOrders(2022);
    double sum = efoodOrders.stream().mapToDouble(o -> Double.parseDouble(o.getPrice())).sum();
    builder.append(String.format("Efood Orders for 2022: %d orders, total cost: %f", efoodOrders.size(), sum));

    builder.append("\n\r");

    WoltService woltService = new WoltService(googleServiceProvider, oauth2AuthorizedClientProvider, htmlParser);
    List<WoltOrder> woltOrders = woltService.getOrders(2022);
    sum = woltOrders.stream().mapToDouble(o -> Double.parseDouble(o.getPrice())).sum();
    builder.append(String.format("Wolt Orders for 2022: %d orders, total cost: %f", woltOrders.size(), sum));

    return builder.toString();
  }
}