package com.gmail.scanner.service.parser;

import com.gmail.scanner.service.model.FoodOrder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public interface OrderParser {

  FoodOrder parseOrder(String emailBody);

  default LocalDateTime parseDateWithPattern(String date, String pattern) {
    try {
      return LocalDateTime.parse(date, DateTimeFormatter.ofPattern(pattern));
    } catch (DateTimeParseException dateTimeParseException) {
      return null;
    }
  }

  default boolean isFoodOrderComplete(FoodOrder foodOrder) {
    return foodOrder.getPrice() != null && foodOrder.getOrderDate() != null;
  }

}
