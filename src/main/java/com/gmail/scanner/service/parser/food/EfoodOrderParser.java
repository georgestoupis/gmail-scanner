package com.gmail.scanner.service.parser.food;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmail.scanner.service.model.FoodOrder;
import com.gmail.scanner.service.model.Source;
import com.gmail.scanner.service.parser.EmailData;
import com.gmail.scanner.service.parser.OrderParser;
import java.time.LocalDateTime;
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
  public FoodOrder parseOrder(EmailData emailData, Source source, LocalDateTime orderDateTime) {
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
      if (orderJson.isEmpty()) {
        return null;
      }
      FoodOrder foodOrder = mapper.readValue(orderJson.get(), FoodOrder.class);
      foodOrder.setSource(source);
      foodOrder.setDate(orderDateTime);
      return foodOrder;
    } catch (JsonProcessingException jsonProcessingException) {
      return null;
    }
  }
}
