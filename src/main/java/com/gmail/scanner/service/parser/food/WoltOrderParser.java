package com.gmail.scanner.service.parser.food;

import com.gmail.scanner.service.model.FoodOrder;
import com.gmail.scanner.service.model.Source;
import com.gmail.scanner.service.parser.EmailData;
import com.gmail.scanner.service.parser.OrderParser;
import com.google.common.base.CharMatcher;
import java.time.LocalDateTime;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WoltOrderParser implements OrderParser {

  private static final String WOLT_PRICE_LABEL = "EUR";

  @Override
  public FoodOrder parseOrder(EmailData emailData, Source source, LocalDateTime orderDateTime) {
    Document document = Jsoup.parse(emailData.html());
    List<String> texts = document.getElementsByTag("td").eachText().stream().map(s -> CharMatcher.ascii().retainFrom(s)).toList();
    FoodOrder foodOrder = new FoodOrder();
    for (int i = 0; i < texts.size() - 1; i++) {
      if (texts.get(i).contains(WOLT_PRICE_LABEL) && this.foundTotalPrice(foodOrder, texts.get(i + 1).replace(",", "."))) {
        foodOrder.setPrice(this.normalizePrice(texts.get(i + 1)));
      }
    }
    foodOrder.setSource(source);
    foodOrder.setDate(orderDateTime);
    return foodOrder;
  }
}
