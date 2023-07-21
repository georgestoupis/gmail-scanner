package com.gmail.scanner.service.parser;

import com.gmail.scanner.service.model.FoodOrder;
import com.gmail.scanner.service.model.FoodOrderSource;
import org.springframework.stereotype.Component;

@Component
public class HtmlParser {

  private final WoltOrderParser woltOrderParser;
  private final BoxOrderParser boxOrderParser;
  private final EfoodOrderParser efoodOrderParser;

  public HtmlParser(WoltOrderParser woltOrderParser, BoxOrderParser boxOrderParser, EfoodOrderParser efoodOrderParser) {
    this.woltOrderParser = woltOrderParser;
    this.boxOrderParser = boxOrderParser;
    this.efoodOrderParser = efoodOrderParser;
  }

  public FoodOrder parserOrder(String emailBody, FoodOrderSource source) {
    return switch (source) {
      case EFOOD -> efoodOrderParser.parseOrder(emailBody);
      case WOLT -> woltOrderParser.parseOrder(emailBody);
      case BOX -> boxOrderParser.parseOrder(emailBody);
    };
  }

}
