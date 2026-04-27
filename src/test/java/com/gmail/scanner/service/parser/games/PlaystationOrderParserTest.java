package com.gmail.scanner.service.parser.games;

import static org.assertj.core.api.Assertions.assertThat;

import com.gmail.scanner.service.parser.EmailData;
import org.junit.jupiter.api.Test;

class PlaystationOrderParserTest {

  private final PlaystationOrderParser parser = new PlaystationOrderParser();

  @Test
  void parseOrderPrice_extractsInlinePriceFromTotalLabel() {
    String html = "<table><tr><td>Total: 12.99</td></tr></table>";
    assertThat(parser.parseOrderPrice(new EmailData(html, null, null))).hasValue("12.99");
  }

  @Test
  void parseOrderPrice_prefersPayloadOverHtml() {
    String payload = "<table><tr><td>Total: 12.99</td></tr></table>";
    String html = "<table><tr><td>Total: 5.00</td></tr></table>";
    assertThat(parser.parseOrderPrice(new EmailData(payload, null, html))).hasValue("12.99");
  }

  @Test
  void parseOrderPrice_returnsEmptyWhenNoTotalLabel() {
    String html = "<table><tr><td>Amount: 12.99</td></tr></table>";
    assertThat(parser.parseOrderPrice(new EmailData(html, null, null))).isEmpty();
  }
}
