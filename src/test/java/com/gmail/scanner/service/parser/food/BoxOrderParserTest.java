package com.gmail.scanner.service.parser.food;

import static org.assertj.core.api.Assertions.assertThat;

import com.gmail.scanner.service.parser.EmailData;
import org.junit.jupiter.api.Test;

class BoxOrderParserTest {

  private final BoxOrderParser parser = new BoxOrderParser();

  @Test
  void parseOrderPrice_extractsPriceAfterLabel() {
    String html = "<table><tr><td>Τελικό ποσό</td><td>12,50</td></tr></table>";
    assertThat(parser.parseOrderPrice(new EmailData(html, null, null))).hasValue("12.50");
  }

  @Test
  void parseOrderPrice_returnsEmptyWhenLabelAbsent() {
    String html = "<table><tr><td>Σύνολο</td><td>12,50</td></tr></table>";
    assertThat(parser.parseOrderPrice(new EmailData(html, null, null))).isEmpty();
  }
}
