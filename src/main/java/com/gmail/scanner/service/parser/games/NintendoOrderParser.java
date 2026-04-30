package com.gmail.scanner.service.parser.games;

import static com.gmail.scanner.service.parser.ParserUtils.normalizePrice;

import com.gmail.scanner.service.parser.EmailData;
import com.gmail.scanner.service.parser.OrderParser;
import java.util.List;
import java.util.Optional;

public class NintendoOrderParser implements OrderParser {

  private static final String PRICE_PREFIX = "Total Charge";

  @Override
  public Optional<String> parseOrderPrice(EmailData emailData) {
    List<String> lines = emailData.plain().lines().filter(x -> !x.isBlank()).toList();
    for (int i = 0; i < lines.size() - 1; i++) {
      if (lines.get(i).contains(PRICE_PREFIX)) {
        return Optional.of(normalizePrice(lines.get(i + 1)));
      }
    }
    return Optional.empty();
  }
}
