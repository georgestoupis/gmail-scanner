package com.gmail.scanner.service.parser;

import com.google.common.base.CharMatcher;
import java.util.List;
import org.jsoup.nodes.Document;

public final class ParserUtils {

  private ParserUtils() {
  }

  public static String normalizePrice(String price, String... substringsToRemove) {
    price = ParserUtils.removeSubstrings(price, "€", "$", "£", "EUR", "USD", "GBP");
    price = ParserUtils.removeSubstrings(price, substringsToRemove);
    return price.replace(",", ".").replaceAll("[\\p{C}\\p{Z}]", "").trim();
  }

  public static boolean foundTotalPrice(String currentPrice, String newPrice) {
    try {
      double parsed = Double.parseDouble(newPrice);
      if (currentPrice == null) {
        return true;
      }
      return Double.parseDouble(currentPrice) < parsed;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public static List<String> parseHtmlTdElements(Document document) {
    return document.getElementsByTag("td").eachText()
        .stream()
        .map(s -> CharMatcher.javaIsoControl().removeFrom(s))
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
