package com.gmail.scanner.service.parser;

import com.gmail.scanner.service.model.Order;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RiotOrderParser implements OrderParser {

  private static final String PRICE_PREFIX_2023 = "Total:";
  private static final String PRICE_PREFIX_2022 = "You have paid:";

  @Override
  public Order parseOrder(EmailData emailData) {
    Order order = new Order();
    List<String> lines = emailData.plain().lines().toList();
    String price = null;
    for (int i = 0; i < lines.size() - 1; i++) {
      String line = lines.get(i);
      if (line.contains(PRICE_PREFIX_2023)) {
        price = this.normalizePrice(lines.get(i + 1));
      } else if (line.contains(PRICE_PREFIX_2022)) {
        price = this.normalizePrice(line.replace(PRICE_PREFIX_2022, ""));
      }

      if (price != null) {
        order.setPrice(price);
        break;
      }
    }
    return order;
  }

}
