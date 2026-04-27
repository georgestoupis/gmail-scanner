package com.gmail.scanner.service.parser.games;

import static com.gmail.scanner.service.parser.ParserUtils.normalizePrice;
import static com.gmail.scanner.service.parser.ParserUtils.parseHtmlTdElements;

import com.gmail.scanner.service.parser.EmailData;
import com.gmail.scanner.service.parser.OrderParser;
import java.util.List;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class PlaystationOrderParser implements OrderParser {

  private static final String PRICE_PREFIX_1 = "Total:";

  @Override
  public Optional<String> parseOrderPrice(EmailData emailData) {
    Document document = Jsoup.parse(emailData.payload() != null ? emailData.payload() : emailData.html());
    List<String> texts = parseHtmlTdElements(document);
    String price = null;
    for (int i = 0; i < texts.size() - 1; i++) {
      if (texts.get(i).contains(PRICE_PREFIX_1)) {
        price = normalizePrice(texts.get(i), PRICE_PREFIX_1);
      }
    }
    return Optional.ofNullable(price);
  }
}
