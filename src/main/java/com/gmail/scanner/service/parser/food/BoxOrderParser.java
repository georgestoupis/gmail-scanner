package com.gmail.scanner.service.parser.food;

import static com.gmail.scanner.service.parser.ParserUtils.normalizePrice;

import com.gmail.scanner.service.parser.EmailData;
import com.gmail.scanner.service.parser.OrderParser;
import java.util.List;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class BoxOrderParser implements OrderParser {

  private static final String BOX_PRICE_LABEL = "Τελικό ποσό";

  @Override
  public Optional<String> parseOrderPrice(EmailData emailData) {
    Document document = Jsoup.parse(emailData.payload());
    List<String> texts = document.getElementsByTag("td").eachText();
    String price = null;
    for (int i = 0; i < texts.size() - 1; i++) {
      if (texts.get(i).equals(BOX_PRICE_LABEL)) {
        price = normalizePrice(texts.get(i + 1));
      }
    }
    return Optional.ofNullable(price);
  }
}
