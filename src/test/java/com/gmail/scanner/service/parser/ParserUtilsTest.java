package com.gmail.scanner.service.parser;

import static org.assertj.core.api.Assertions.assertThat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

class ParserUtilsTest {

  @Test
  void normalizePrice_removesCurrencySymbols() {
    assertThat(ParserUtils.normalizePrice("12.50€")).isEqualTo("12.50");
    assertThat(ParserUtils.normalizePrice("$12.50")).isEqualTo("12.50");
    assertThat(ParserUtils.normalizePrice("12.50 EUR")).isEqualTo("12.50");
    assertThat(ParserUtils.normalizePrice("USD 12.50")).isEqualTo("12.50");
  }

  @Test
  void normalizePrice_convertsCommaToDecimalPoint() {
    assertThat(ParserUtils.normalizePrice("12,50")).isEqualTo("12.50");
  }

  @Test
  void normalizePrice_trimsWhitespace() {
    assertThat(ParserUtils.normalizePrice("  12.50  ")).isEqualTo("12.50");
  }

  @Test
  void normalizePrice_removesAdditionalSubstrings() {
    assertThat(ParserUtils.normalizePrice("Total: 12.50", "Total:")).isEqualTo("12.50");
  }

  @Test
  void foundTotalPrice_returnsTrueWhenCurrentPriceIsNull() {
    assertThat(ParserUtils.foundTotalPrice(null, "12.50")).isTrue();
  }

  @Test
  void foundTotalPrice_returnsFalseWhenNewPriceIsNotParsable() {
    assertThat(ParserUtils.foundTotalPrice(null, "not-a-number")).isFalse();
    assertThat(ParserUtils.foundTotalPrice("10.00", "not-a-number")).isFalse();
  }

  @Test
  void foundTotalPrice_returnsTrueWhenNewPriceIsHigher() {
    assertThat(ParserUtils.foundTotalPrice("10.00", "15.00")).isTrue();
  }

  @Test
  void foundTotalPrice_returnsFalseWhenNewPriceIsLowerOrEqual() {
    assertThat(ParserUtils.foundTotalPrice("15.00", "10.00")).isFalse();
    assertThat(ParserUtils.foundTotalPrice("10.00", "10.00")).isFalse();
  }

  @Test
  void parseHtmlTdElements_extractsTdText() {
    Document doc = Jsoup.parse("<table><tr><td>Hello</td><td>World</td></tr></table>");
    assertThat(ParserUtils.parseHtmlTdElements(doc)).containsExactly("Hello", "World");
  }

  @Test
  void parseHtmlTdElements_stripsNonAsciiCharacters() {
    Document doc = Jsoup.parse("<table><tr><td>€12.50</td></tr></table>");
    assertThat(ParserUtils.parseHtmlTdElements(doc)).containsExactly("12.50");
  }

  @Test
  void parseHtmlTdElements_stripsGreekCharacters() {
    Document doc = Jsoup.parse("<table><tr><td>Τελικό ποσό</td></tr></table>");
    assertThat(ParserUtils.parseHtmlTdElements(doc)).containsExactly("");
  }

  @Test
  void parseHtmlTdElements_trimsWhitespace() {
    Document doc = Jsoup.parse("<table><tr><td>  hello  </td></tr></table>");
    assertThat(ParserUtils.parseHtmlTdElements(doc)).containsExactly("hello");
  }
}
