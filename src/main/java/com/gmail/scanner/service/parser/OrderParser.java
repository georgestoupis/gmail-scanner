package com.gmail.scanner.service.parser;

import com.gmail.scanner.service.model.Order;
import org.apache.commons.lang3.math.NumberUtils;

public interface OrderParser {

  Order parseOrder(EmailData emailData);

  default String normalizePrice(String price) {
    return this.removeSubstrings(price, "€", "$", "EUR").replace(",", ".").trim();
  }

  default String removeSubstrings(String string, String... substrings) {
    for (String substring : substrings) {
      string = string.replace(substring, "");
    }
    return string;
  }

  default boolean foundTotalPrice(Order order, String priceText) {
    return NumberUtils.isParsable(priceText) && (order.getPrice() == null || Double.parseDouble(order.getPrice()) < Double.parseDouble(priceText));
  }

}
