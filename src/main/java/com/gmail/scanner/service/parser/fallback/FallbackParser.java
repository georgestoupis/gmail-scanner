package com.gmail.scanner.service.parser.fallback;

import static com.gmail.scanner.service.parser.ParserUtils.foundTotalPrice;
import static com.gmail.scanner.service.parser.ParserUtils.normalizePrice;
import static com.gmail.scanner.service.parser.ParserUtils.parseHtmlTdElements;

import com.gmail.scanner.service.parser.EmailData;
import com.gmail.scanner.service.parser.OrderParser;
import java.util.List;
import java.util.Optional;
import org.jsoup.Jsoup;

public class FallbackParser implements OrderParser {

  private static final List<String> PRICE_PREFIX_LIST = List.of(
      "Grand Total", "Total Charge", "Order Total", "Total Amount", "Total Due", "Total",
      "Amount Charged", "Amount Due", "You were charged", "Payment Total",
      "Τελικό ποσό", "Συνολικό ποσό", "Πληρωτέο ποσό", "Σύνολο");

  @Override
  public Optional<String> parseOrderPrice(EmailData emailData) {
    return parseFromPlain(emailData.plain())
        .or(() -> parseFromHtml(emailData.html()));
  }

  private Optional<String> parseFromPlain(String plain) {
    return Optional.ofNullable(plain)
        .flatMap(p -> scanForPrice(p.lines().filter(x -> !x.isBlank()).toList()));
  }

  private Optional<String> parseFromHtml(String html) {
    return Optional.ofNullable(html)
        .flatMap(h -> scanForPrice(parseHtmlTdElements(Jsoup.parse(h))));
  }

  private Optional<String> scanForPrice(List<String> strings) {
    String price = null;
    for (int i = 0; i < strings.size() - 1; i++) {
      String string = strings.get(i);
      if (PRICE_PREFIX_LIST.stream().anyMatch(string::contains)) {
        String candidate = normalizePrice(strings.get(i + 1));
        if (foundTotalPrice(price, candidate)) {
          price = candidate;
        }
      }
    }
    return Optional.ofNullable(price);
  }
}