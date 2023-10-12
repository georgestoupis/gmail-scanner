package com.gmail.scanner.service.parser;

import com.gmail.scanner.service.model.FoodOrder;
import com.google.common.base.CharMatcher;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class WoltOrderParser implements OrderParser {

  private static final String WOLT_PRICE_LABEL = "Total in EUR";

  @Override
  public FoodOrder parseOrder(EmailData emailData) {
    Document document = Jsoup.parse(emailData.html());
    List<String> texts = document.getElementsByTag("td").eachText().stream().map(s -> CharMatcher.ascii().retainFrom(s)).toList();
    FoodOrder foodOrder = new FoodOrder();
    for (int i = 0; i < texts.size() - 1; i++) {
      if (texts.get(i).contains(WOLT_PRICE_LABEL) && this.foundTotalPrice(foodOrder, texts.get(i + 1))) {
        foodOrder.setPrice(this.normalizePrice(texts.get(i + 1)));
      }
    }
    return foodOrder;
  }

}
