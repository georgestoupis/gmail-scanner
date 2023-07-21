package com.gmail.scanner.service.parser;

import com.gmail.scanner.service.model.FoodOrder;
import com.google.common.base.CharMatcher;
import java.time.LocalDateTime;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class WoltOrderParser implements OrderParser {

  private static final String WOLT_DATE_PATTERN = "dd.MM.yyyy HH:mm";
  private static final String WOLT_DATE_PATTERN_2 = "EEE dd MMM HH:mm:ss yyyy";
  private static final String WOLT_PRICE_LABEL = "Total in EUR";
  private static final String WOLT_DATE_LABEL = "Delivery time";

  @Override
  public FoodOrder parseOrder(String emailBody) {
    Document document = Jsoup.parse(emailBody);
    List<String> texts = document.getElementsByTag("td").eachText().stream().map(s -> CharMatcher.ascii().retainFrom(s)).toList();
    FoodOrder foodOrder = new FoodOrder();
    //Yes this is flaky af, no I don't currently care
    for (int i = 0; i < texts.size(); i++) {
      if (texts.get(i).contains(WOLT_PRICE_LABEL)) {
        if (foodOrder.getPrice() == null || Double.parseDouble(foodOrder.getPrice()) < Double.parseDouble(texts.get(i + 1))) {
          foodOrder.setPrice(texts.get(i + 1));
        }
      } else if (WOLT_DATE_LABEL.equals(texts.get(i))) {
        String date = texts.get(i + 1);
        LocalDateTime orderDate = this.parseDate(date);
        foodOrder.setOrderDate(orderDate);
      }
      if (this.isFoodOrderComplete(foodOrder)) {
        break;
      }
    }
    return foodOrder;
  }

  private LocalDateTime parseDate(String dateText) {
    if ("ASAP".equals(dateText)) {
      return null;
    }
    LocalDateTime orderDateDefaultPattern = this.parseDateWithPattern(dateText, WOLT_DATE_PATTERN);
    return orderDateDefaultPattern != null ? orderDateDefaultPattern : this.parseDateWithPattern(dateText, WOLT_DATE_PATTERN_2);
  }

}
