package com.gmail.scanner.service.parser.food;

import static com.gmail.scanner.service.parser.ParserUtils.foundTotalPrice;
import static com.gmail.scanner.service.parser.ParserUtils.normalizePrice;
import static com.gmail.scanner.service.parser.ParserUtils.parseHtmlTdElements;

import com.gmail.scanner.service.parser.EmailData;
import com.gmail.scanner.service.parser.OrderParser;
import java.util.List;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WoltOrderParser implements OrderParser {

  private static final String WOLT_PRICE_LABEL = "EUR";

  @Override
  public Optional<String> parseOrderPrice(EmailData emailData) {
    Document document = Jsoup.parse(emailData.html());
    List<String> texts = parseHtmlTdElements(document);
    String price = null;
    for (int i = 0; i < texts.size() - 1; i++) {
      if (texts.get(i).contains(WOLT_PRICE_LABEL)) {
        var priceText = normalizePrice(texts.get(i), WOLT_PRICE_LABEL);
        if (foundTotalPrice(price, priceText)) {
          price = priceText;
        }
        var nextLine = normalizePrice(texts.get(i + 1), WOLT_PRICE_LABEL);
        if (foundTotalPrice(price, nextLine)) {
          price = nextLine;
        }
      }
    }
    return Optional.ofNullable(price);
  }
}
