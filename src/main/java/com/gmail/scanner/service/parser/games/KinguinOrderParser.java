package com.gmail.scanner.service.parser.games;

import static com.gmail.scanner.service.parser.ParserUtils.foundTotalPrice;
import static com.gmail.scanner.service.parser.ParserUtils.normalizePrice;
import static com.gmail.scanner.service.parser.ParserUtils.parseHtmlTdElements;

import com.gmail.scanner.service.parser.EmailData;
import com.gmail.scanner.service.parser.OrderParser;
import java.util.List;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class KinguinOrderParser implements OrderParser {

  private static final String PRICE_PREFIX = "Total";

  @Override
  public Optional<String> parseOrderPrice(EmailData emailData) {
    Document document = Jsoup.parse(emailData.html());
    List<String> texts = parseHtmlTdElements(document);
    String price = null;
    for (int i = 0; i < texts.size() - 1; i++) {
      if (texts.get(i).contains(PRICE_PREFIX)) {
        String next = normalizePrice(texts.get(i + 1));
        if (foundTotalPrice(price, next)) {
          price = next;
        }
      }
    }
    return Optional.ofNullable(price);
  }
}
