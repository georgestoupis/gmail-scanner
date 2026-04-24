package com.gmail.scanner.service.parser.games;

import com.gmail.scanner.service.model.Order;
import com.gmail.scanner.service.model.Source;
import com.gmail.scanner.service.parser.EmailData;
import com.gmail.scanner.service.parser.OrderParser;
import com.google.common.base.CharMatcher;
import java.time.LocalDateTime;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class SteamOrderParser implements OrderParser {

  private static final String PRICE_PREFIX_1 = "Your total for this transaction:";
  private static final String PRICE_PREFIX_2 = "Total (including 24% tax):";

  @Override
  public Order parseOrder(EmailData emailData, Source source, LocalDateTime orderDateTime) {
    Document document = Jsoup.parse(emailData.html());
    List<String> texts = document.getElementsByTag("td").eachText().stream().map(s -> CharMatcher.ascii().retainFrom(s)).toList();
    Order order = new Order();
    for (int i = 0; i < texts.size() - 1; i++) {
      String current = texts.get(i);
      String next = texts.get(i + 1);
      if ((current.contains(PRICE_PREFIX_1) || current.contains(PRICE_PREFIX_2)) && this.foundTotalPrice(order, this.normalizePrice(next))) {
        order.setPrice(this.normalizePrice(next));
      }
    }
    order.setSource(source);
    order.setDate(orderDateTime);
    return order;
  }
}
