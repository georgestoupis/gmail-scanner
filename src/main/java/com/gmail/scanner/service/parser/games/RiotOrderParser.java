package com.gmail.scanner.service.parser.games;

import static com.gmail.scanner.service.parser.ParserUtils.normalizePrice;

import com.gmail.scanner.service.model.Order;
import com.gmail.scanner.service.model.Source;
import com.gmail.scanner.service.parser.EmailData;
import com.gmail.scanner.service.parser.OrderParser;
import java.time.LocalDateTime;
import java.util.List;

public class RiotOrderParser implements OrderParser {

  private static final String PRICE_PREFIX_2023 = "Total:";
  private static final String PRICE_PREFIX_2022 = "You have paid:";

  @Override
  public Order parseOrder(EmailData emailData, Source source, LocalDateTime orderDateTime) {
    Order order = new Order();
    List<String> lines = emailData.plain().lines().toList();
    String price = null;
    for (int i = 0; i < lines.size() - 1; i++) {
      String line = lines.get(i);
      if (line.contains(PRICE_PREFIX_2023)) {
        price = normalizePrice(lines.get(i + 1));
      } else if (line.contains(PRICE_PREFIX_2022)) {
        price = normalizePrice(line, PRICE_PREFIX_2022);
      }

      if (price != null) {
        order.setPrice(price);
        break;
      }
    }
    order.setSource(source);
    order.setDate(orderDateTime);
    return order;
  }
}
