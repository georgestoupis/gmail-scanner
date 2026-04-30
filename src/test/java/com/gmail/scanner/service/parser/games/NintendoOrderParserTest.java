package com.gmail.scanner.service.parser.games;

import static org.assertj.core.api.Assertions.assertThat;

import com.gmail.scanner.service.parser.EmailData;
import org.junit.jupiter.api.Test;

class NintendoOrderParserTest {

  private final NintendoOrderParser parser = new NintendoOrderParser();

  @Test
  void parseOrderPrice_extractsPrice() {
    String plain = "--------------------\nTotal Charge (incl. VAT)\n11,49 €\n--------------------";
    assertThat(parser.parseOrderPrice(new EmailData(null, plain, null))).hasValue("11.49");
  }

  @Test
  void parseOrderPrice_returnsEmptyWhenNoPricePrefix() {
    String plain = "--------------------\nSubtotal\n11,49 €\n--------------------";
    assertThat(parser.parseOrderPrice(new EmailData(null, plain, null))).isEmpty();
  }
}
