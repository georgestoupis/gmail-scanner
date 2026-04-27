package com.gmail.scanner.service.parser;

import com.google.common.base.CharMatcher;
import java.util.List;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.nodes.Document;

public final class ParserUtils {

  private ParserUtils() {
  }

  public static String normalizePrice(String price, String... substringsToRemove) {
    price = ParserUtils.removeSubstrings(price, "€", "$", "EUR", "USD");
    price = ParserUtils.removeSubstrings(price, substringsToRemove);
    return price.replace(",", ".").trim();
  }

  public static boolean foundTotalPrice(String currentPrice, String newPrice) {
    if (!NumberUtils.isParsable(newPrice)) {
      return false;
    }
    if (currentPrice == null) {
      return true;
    }
    return Double.parseDouble(currentPrice) < Double.parseDouble(newPrice);
  }

  public static List<String> parseHtmlTdElements(Document document) {
    return document.getElementsByTag("td").eachText()
        .stream()
        .map(s -> CharMatcher.ascii().retainFrom(s))
        .map(String::trim)
        .toList();
  }

  private static String removeSubstrings(String string, String... substrings) {
    for (String substring : substrings) {
      string = string.replace(substring, "");
    }
    return string;
  }


}
