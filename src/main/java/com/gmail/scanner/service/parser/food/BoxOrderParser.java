package com.gmail.scanner.service.parser.food;

import com.gmail.scanner.service.model.FoodOrder;
import com.gmail.scanner.service.model.Source;
import com.gmail.scanner.service.parser.EmailData;
import com.gmail.scanner.service.parser.OrderParser;
import java.time.LocalDateTime;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class BoxOrderParser implements OrderParser {

  private static final String BOX_PRICE_LABEL = "Τελικό ποσό";

  @Override
  public FoodOrder parseOrder(EmailData emailData, Source source, LocalDateTime orderDateTime) {
    Document document = Jsoup.parse(emailData.payload());
    List<String> texts = document.getElementsByTag("td").eachText();
    FoodOrder foodOrder = new FoodOrder();
    for (int i = 0; i < texts.size() - 1; i++) {
      if (texts.get(i).equals(BOX_PRICE_LABEL)) {
        String price = texts.get(i + 1);
        foodOrder.setPrice(this.normalizePrice(price));
      }
    }
    foodOrder.setSource(source);
    foodOrder.setDate(orderDateTime);
    return foodOrder;
  }
}
