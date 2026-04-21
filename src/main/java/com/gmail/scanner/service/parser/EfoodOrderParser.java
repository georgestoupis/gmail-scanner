package com.gmail.scanner.service.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmail.scanner.service.model.FoodOrder;
import java.util.Objects;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class EfoodOrderParser implements OrderParser {

  private final ObjectMapper mapper;

  public EfoodOrderParser(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public FoodOrder parseOrder(EmailData emailData) {
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
    try {
      return orderJson.isEmpty() ? null : mapper.readValue(orderJson.get(), FoodOrder.class);
    } catch (JsonProcessingException jsonProcessingException) {
      return null;
    }
  }
}
