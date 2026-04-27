package com.gmail.scanner.service.parser.travel;

import static org.assertj.core.api.Assertions.assertThat;

import com.gmail.scanner.service.parser.EmailData;
import org.junit.jupiter.api.Test;

class UberOrderParserTest {

  private final UberOrderParser parser = new UberOrderParser();

  @Test
  void parseOrderPrice_extractsPriceAfterExactTotalLabel() {
    String html = "<table><tr><td>Total</td><td>12.50</td></tr></table>";
    assertThat(parser.parseOrderPrice(new EmailData(html, null, null))).hasValue("12.50");
  }

  @Test
  void parseOrderPrice_doesNotMatchPartialLabel() {
    String html = "<table><tr><td>Subtotal</td><td>12.50</td></tr></table>";
    assertThat(parser.parseOrderPrice(new EmailData(html, null, null))).isEmpty();
  }

  @Test
  void parseOrderPrice_returnsEmptyWhenNoTotalLabel() {
    String html = "<table><tr><td>Amount</td><td>12.50</td></tr></table>";
    assertThat(parser.parseOrderPrice(new EmailData(html, null, null))).isEmpty();
  }
}
