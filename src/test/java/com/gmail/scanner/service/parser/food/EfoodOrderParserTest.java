package com.gmail.scanner.service.parser.food;

import static org.assertj.core.api.Assertions.assertThat;

import com.gmail.scanner.service.parser.EmailData;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class EfoodOrderParserTest {

  private final EfoodOrderParser parser = new EfoodOrderParser();

  @Test
  void parseOrderPrice_extractsPriceFromScriptTag() throws IOException {
    String html = loadResource("efood-order.html");
    assertThat(parser.parseOrderPrice(new EmailData(html, null, null))).hasValue("12.50");
  }

  @Test
  void parseOrderPrice_returnsEmptyWhenNoScriptTag() {
    String html = "<html><body><p>No script here</p></body></html>";
    assertThat(parser.parseOrderPrice(new EmailData(html, null, null))).isEmpty();
  }

  @Test
  void parseOrderPrice_returnsEmptyWhenScriptJsonIsMalformed() {
    String html = "<html><body><script>not valid json</script></body></html>";
    assertThat(parser.parseOrderPrice(new EmailData(html, null, null))).isEmpty();
  }

  private String loadResource(String filename) throws IOException {
    try (var stream = getClass().getResourceAsStream("/parser/" + filename)) {
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}
