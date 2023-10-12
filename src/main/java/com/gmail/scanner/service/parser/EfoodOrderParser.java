package com.gmail.scanner.service.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmail.scanner.service.model.FoodOrder;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EfoodOrderParser implements OrderParser {

  @Autowired
  private ObjectMapper mapper;

  @Override
  public FoodOrder parseOrder(EmailData emailData) {
    Document document = Jsoup.parse(emailData.payload() != null ? emailData.payload() : emailData.html());
    Elements scripts = document.getElementsByTag("script");
    Optional<String> orderJson = scripts.stream().map(s -> s.childNodes().stream().findFirst().get().toString()).findFirst();
    try {
      return orderJson.isEmpty() ? null : mapper.readValue(orderJson.get(), FoodOrder.class);
    } catch (JsonProcessingException jsonProcessingException) {
      return null;
    }
  }
}
