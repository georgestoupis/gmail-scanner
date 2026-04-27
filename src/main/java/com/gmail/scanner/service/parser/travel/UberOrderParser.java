package com.gmail.scanner.service.parser.travel;

import static com.gmail.scanner.service.parser.ParserUtils.normalizePrice;
import static com.gmail.scanner.service.parser.ParserUtils.parseHtmlTdElements;

import com.gmail.scanner.service.parser.EmailData;
import com.gmail.scanner.service.parser.OrderParser;
import java.util.List;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class UberOrderParser implements OrderParser {

  private static final String PRICE_PREFIX = "Total";

  @Override
  public Optional<String> parseOrderPrice(EmailData emailData) {
    Document document = Jsoup.parse(emailData.payload() != null ? emailData.payload() : emailData.html());
    List<String> texts = parseHtmlTdElements(document);
    String price = null;
    for (int i = 0; i < texts.size() - 1; i++) {
      if (texts.get(i).equals(PRICE_PREFIX)) {
        price = normalizePrice(texts.get(i + 1), PRICE_PREFIX);
      }
    }
    return Optional.ofNullable(price);
  }
}
