package com.gmail.scanner.service.parser.shopping;

import static org.assertj.core.api.Assertions.assertThat;

import com.gmail.scanner.service.parser.EmailData;
import org.junit.jupiter.api.Test;

class SkroutzOrderParserTest {

  private final SkroutzOrderParser parser = new SkroutzOrderParser();

  @Test
  void parseOrderPrice_extractsPriceAfterSynolo() {
    String plain = "Order summary\nΣύνολο\n24.99\nThank you";
    assertThat(parser.parseOrderPrice(new EmailData(null, plain, null))).hasValue("24.99");
  }

  @Test
  void parseOrderPrice_handlesCommaDecimalSeparator() {
    String plain = "Σύνολο\n24,99";
    assertThat(parser.parseOrderPrice(new EmailData(null, plain, null))).hasValue("24.99");
  }

  @Test
  void parseOrderPrice_returnsEmptyWhenLabelAbsent() {
    String plain = "Order summary\nTotal\n24.99";
    assertThat(parser.parseOrderPrice(new EmailData(null, plain, null))).isEmpty();
  }
}
