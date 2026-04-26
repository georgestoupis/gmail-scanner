package com.gmail.scanner.service.parser.games;

import static com.gmail.scanner.service.parser.ParserUtils.foundTotalPrice;
import static com.gmail.scanner.service.parser.ParserUtils.normalizePrice;
import static com.gmail.scanner.service.parser.ParserUtils.parseHtmlTdElements;

import com.gmail.scanner.service.model.Order;
import com.gmail.scanner.service.model.Source;
import com.gmail.scanner.service.parser.EmailData;
import com.gmail.scanner.service.parser.OrderParser;
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
    List<String> texts = parseHtmlTdElements(document);
    Order order = new Order();
    for (int i = 0; i < texts.size() - 1; i++) {
      String current = texts.get(i);
      if (current.contains(PRICE_PREFIX_1) || current.contains(PRICE_PREFIX_2)) {
        String next = normalizePrice(texts.get(i + 1));
        if (foundTotalPrice(order, next)) {
          order.setPrice(next);
        }

      }
    }
    order.setSource(source);
    order.setDate(orderDateTime);
    return order;
  }
}
