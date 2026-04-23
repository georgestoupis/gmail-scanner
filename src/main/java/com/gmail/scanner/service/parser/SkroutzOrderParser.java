package com.gmail.scanner.service.parser;

import com.gmail.scanner.service.model.Order;
import java.util.List;

public class SkroutzOrderParser implements OrderParser {

  private static final String PRICE_PREFIX = "Σύνολο";

  public SkroutzOrderParser() {
  }

  @Override
  public Order parseOrder(EmailData emailData) {
    Order order = new Order();
    List<String> lines = emailData.plain().lines().filter(x -> !x.isBlank()).toList();
    String price = null;
    for (int i = 0; i < lines.size() - 1; i++) {
      String line = lines.get(i);
      if (line.contains(PRICE_PREFIX)) {
        price = this.normalizePrice(lines.get(i + 1));
      }

      if (price != null) {
        order.setPrice(price);
        break;
      }
    }
    return order;
  }
}
