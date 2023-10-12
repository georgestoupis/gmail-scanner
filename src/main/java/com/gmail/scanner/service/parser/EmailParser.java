package com.gmail.scanner.service.parser;

import com.gmail.scanner.service.model.Order;
import com.gmail.scanner.service.model.Source;
import org.springframework.stereotype.Component;

@Component
public class EmailParser {

  private final WoltOrderParser woltOrderParser;
  private final BoxOrderParser boxOrderParser;
  private final EfoodOrderParser efoodOrderParser;
  private final RiotOrderParser riotOrderParser;
  private final SteamOrderParser steamOrderParser;

  public EmailParser(WoltOrderParser woltOrderParser, BoxOrderParser boxOrderParser, EfoodOrderParser efoodOrderParser, RiotOrderParser riotOrderParser,
      SteamOrderParser steamOrderParser) {
    this.woltOrderParser = woltOrderParser;
    this.boxOrderParser = boxOrderParser;
    this.efoodOrderParser = efoodOrderParser;
    this.riotOrderParser = riotOrderParser;
    this.steamOrderParser = steamOrderParser;
  }

  public Order parserOrder(EmailData emailData, Source source) {
    return switch (source) {
      case EFOOD -> efoodOrderParser.parseOrder(emailData);
      case WOLT -> woltOrderParser.parseOrder(emailData);
      case BOX -> boxOrderParser.parseOrder(emailData);
      case RIOT -> riotOrderParser.parseOrder(emailData);
      case STEAM -> steamOrderParser.parseOrder(emailData);
      default -> null;
    };
  }

}
