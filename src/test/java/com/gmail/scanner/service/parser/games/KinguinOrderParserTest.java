package com.gmail.scanner.service.parser.games;

import static org.assertj.core.api.Assertions.assertThat;

import com.gmail.scanner.service.parser.EmailData;
import org.junit.jupiter.api.Test;

class KinguinOrderParserTest {

  private final KinguinOrderParser parser = new KinguinOrderParser();

  @Test
  void parseOrderPrice_extractsPriceAfterTotalLabel() {
    String html = "<table><tr><td>Total</td><td>14.99</td></tr></table>";
    assertThat(parser.parseOrderPrice(new EmailData(null, null, html))).hasValue("14.99");
  }

  @Test
  void parseOrderPrice_returnsHighestWhenMultipleTotalLabels() {
    String html = "<table>"
        + "<tr><td>Subtotal</td><td>10.00</td></tr>"
        + "<tr><td>Total</td><td>14.99</td></tr>"
        + "</table>";
    assertThat(parser.parseOrderPrice(new EmailData(null, null, html))).hasValue("14.99");
  }

  @Test
  void parseOrderPrice_returnsEmptyWhenNoTotalLabel() {
    String html = "<table><tr><td>Amount</td><td>14.99</td></tr></table>";
    assertThat(parser.parseOrderPrice(new EmailData(null, null, html))).isEmpty();
  }
}
