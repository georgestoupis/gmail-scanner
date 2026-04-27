package com.gmail.scanner.service.parser.food;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmail.scanner.service.parser.EmailData;
import com.gmail.scanner.service.parser.OrderParser;
import java.util.Objects;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class EfoodOrderParser implements OrderParser {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record EfoodData(String price) {}

  @Override
  public Optional<String> parseOrderPrice(EmailData emailData) {
    Document document = Jsoup.parse(emailData.payload() != null ? emailData.payload() : emailData.html());
    Elements scripts = document.getElementsByTag("script");
    Optional<String> orderJson = scripts.stream()
        .map(s -> s.childNodes()
            .stream()
            .findFirst()
            .map(Node::toString)
            .orElse(null))
        .filter(Objects::nonNull)
        .findFirst();
    if (orderJson.isEmpty()) {
      return Optional.empty();
    }
    try {
      return Optional.ofNullable(MAPPER.readValue(orderJson.get(), EfoodData.class).price());
    } catch (JsonProcessingException e) {
      return Optional.empty();
    }
  }
}
