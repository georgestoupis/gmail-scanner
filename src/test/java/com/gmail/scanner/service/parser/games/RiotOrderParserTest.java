package com.gmail.scanner.service.parser.games;

import static org.assertj.core.api.Assertions.assertThat;

import com.gmail.scanner.service.parser.EmailData;
import org.junit.jupiter.api.Test;

class RiotOrderParserTest {

  private final RiotOrderParser parser = new RiotOrderParser();

  @Test
  void parseOrderPrice_extracts2023Format() {
    String plain = "Order details\nTotal:\n9.99\nThank you";
    assertThat(parser.parseOrderPrice(new EmailData(null, plain, null))).hasValue("9.99");
  }

  @Test
  void parseOrderPrice_extracts2022Format() {
    String plain = "Order details\nYou have paid: 9.99\nThank you";
    assertThat(parser.parseOrderPrice(new EmailData(null, plain, null))).hasValue("9.99");
  }

  @Test
  void parseOrderPrice_returnsEmptyWhenNoPricePrefix() {
    String plain = "Order details\nAmount: 9.99\nThank you";
    assertThat(parser.parseOrderPrice(new EmailData(null, plain, null))).isEmpty();
  }
}
