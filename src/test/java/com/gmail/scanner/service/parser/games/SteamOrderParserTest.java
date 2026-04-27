package com.gmail.scanner.service.parser.games;

import static org.assertj.core.api.Assertions.assertThat;

import com.gmail.scanner.service.parser.EmailData;
import org.junit.jupiter.api.Test;

class SteamOrderParserTest {

  private final SteamOrderParser parser = new SteamOrderParser();

  @Test
  void parseOrderPrice_extractsPriceWithTransactionPrefix() {
    String html = "<table><tr><td>Your total for this transaction:</td><td>12.50</td></tr></table>";
    assertThat(parser.parseOrderPrice(new EmailData(null, null, html))).hasValue("12.50");
  }

  @Test
  void parseOrderPrice_extractsPriceWithTaxPrefix() {
    String html = "<table><tr><td>Total (including 24% tax):</td><td>9.99</td></tr></table>";
    assertThat(parser.parseOrderPrice(new EmailData(null, null, html))).hasValue("9.99");
  }

  @Test
  void parseOrderPrice_returnsHighestWhenBothPrefixesPresent() {
    String html = "<table>"
        + "<tr><td>Your total for this transaction:</td><td>9.99</td></tr>"
        + "<tr><td>Total (including 24% tax):</td><td>12.50</td></tr>"
        + "</table>";
    assertThat(parser.parseOrderPrice(new EmailData(null, null, html))).hasValue("12.50");
  }

  @Test
  void parseOrderPrice_returnsEmptyWhenNoPricePrefix() {
    String html = "<table><tr><td>Order summary</td><td>12.50</td></tr></table>";
    assertThat(parser.parseOrderPrice(new EmailData(null, null, html))).isEmpty();
  }
}
