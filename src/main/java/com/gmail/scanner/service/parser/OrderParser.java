package com.gmail.scanner.service.parser;

import com.gmail.scanner.service.model.Order;

public interface OrderParser {

  Order parseOrder(String emailBody);

  default String normalizePrice(String price) {
    return price.replace(",", ".").replace(" €", "");
  }

}
