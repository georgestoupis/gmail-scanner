package com.gmail.scanner.service.parser;

import com.gmail.scanner.service.model.Order;
import com.google.common.base.CharMatcher;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class KinguinOrderParser implements OrderParser {

  private static final String PRICE_PREFIX_1 = "Total";

  @Override
  public Order parseOrder(EmailData emailData) {
    Document document = Jsoup.parse(emailData.html());
    List<String> texts = document.getElementsByTag("td").eachText().stream().map(s -> CharMatcher.ascii().retainFrom(s)).toList();
    Order order = new Order();
    for (int i = 0; i < texts.size() - 1; i++) {
      String current = texts.get(i);
      String next = texts.get(i + 1);
      if (current.contains(PRICE_PREFIX_1) && this.foundTotalPrice(order, this.normalizePrice(next))) {
        order.setPrice(this.normalizePrice(next));
      }
    }
    return order;
  }
}
