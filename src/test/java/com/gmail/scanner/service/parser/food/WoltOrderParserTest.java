package com.gmail.scanner.service.parser.food;

import static org.assertj.core.api.Assertions.assertThat;

import com.gmail.scanner.service.parser.EmailData;
import org.junit.jupiter.api.Test;

class WoltOrderParserTest {

  private final WoltOrderParser parser = new WoltOrderParser();

  @Test
  void parseOrderPrice_extractsPriceOnSameLine() {
    String html = "<table><tr><td>EUR 12,50</td><td>ignored</td></tr></table>";
    assertThat(parser.parseOrderPrice(new EmailData(null, null, html))).hasValue("12.50");
  }

  @Test
  void parseOrderPrice_extractsPriceFromNextLine() {
    String html = "<table><tr><td>EUR</td><td>12,50</td></tr></table>";
    assertThat(parser.parseOrderPrice(new EmailData(null, null, html))).hasValue("12.50");
  }

  @Test
  void parseOrderPrice_returnsHighestWhenMultiplePricesFound() {
    String html = "<table>"
        + "<tr><td>EUR 5,00</td></tr>"
        + "<tr><td>EUR 18,90</td></tr>"
        + "<tr><td>EUR 3,00</td></tr>"
        + "</table>";
    assertThat(parser.parseOrderPrice(new EmailData(null, null, html))).hasValue("18.90");
  }

  @Test
  void parseOrderPrice_returnsEmptyWhenNoEurLabel() {
    String html = "<table><tr><td>12,50</td></tr></table>";
    assertThat(parser.parseOrderPrice(new EmailData(null, null, html))).isEmpty();
  }
}
