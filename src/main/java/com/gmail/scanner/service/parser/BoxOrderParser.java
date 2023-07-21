package com.gmail.scanner.service.parser;

import com.gmail.scanner.service.model.FoodOrder;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class BoxOrderParser implements OrderParser {

  private static final String BOX_DATE_PATTERN = "dd/MM/yyyy, HH:mm";
  private static final String BOX_PRICE_LABEL = "Τελικό ποσό";
  private static final String BOX_DATE_LABEL = "Ημερομηνία / Ώρα";

  @Override
  public FoodOrder parseOrder(String emailBody) {
    Document document = Jsoup.parse(emailBody);
    List<String> texts = document.getElementsByTag("td").eachText();
    FoodOrder foodOrder = new FoodOrder();
    //Yes this is flaky af, no I don't currently care
    for (int i = 0; i < texts.size(); i++) {
      if (texts.get(i).equals(BOX_PRICE_LABEL)) {
        String price = texts.get(i + 1);
        foodOrder.setPrice(price.replace(",", ".").replace(" €", ""));
      } else if (texts.get(i).equals(BOX_DATE_LABEL)) {
        String date = texts.get(i + 1);
        foodOrder.setOrderDate(this.parseDateWithPattern(date, BOX_DATE_PATTERN));
      }
      if (this.isFoodOrderComplete(foodOrder)) {
        break;
      }
    }
    return foodOrder;
  }
}
