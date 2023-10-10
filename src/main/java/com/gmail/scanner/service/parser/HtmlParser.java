package com.gmail.scanner.service.parser;

import com.gmail.scanner.service.model.Order;
import com.gmail.scanner.service.model.Source;
import org.springframework.stereotype.Component;

@Component
public class HtmlParser {

  private final WoltOrderParser woltOrderParser;
  private final BoxOrderParser boxOrderParser;
  private final EfoodOrderParser efoodOrderParser;
  private final RiotOrderParser riotOrderParser;

  public HtmlParser(WoltOrderParser woltOrderParser, BoxOrderParser boxOrderParser, EfoodOrderParser efoodOrderParser, RiotOrderParser riotOrderParser) {
    this.woltOrderParser = woltOrderParser;
    this.boxOrderParser = boxOrderParser;
    this.efoodOrderParser = efoodOrderParser;
    this.riotOrderParser = riotOrderParser;
  }

  public Order parserOrder(String emailBody, Source source) {
    return switch (source) {
      case EFOOD -> efoodOrderParser.parseOrder(emailBody);
      case WOLT -> woltOrderParser.parseOrder(emailBody);
      case BOX -> boxOrderParser.parseOrder(emailBody);
      case RIOT -> riotOrderParser.parseOrder(emailBody);
      default -> null;
    };
  }

}
