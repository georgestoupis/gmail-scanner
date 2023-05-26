package com.gmail.scanner.service.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmail.scanner.service.model.FoodOrder;
import com.gmail.scanner.service.model.FoodOrderSource;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
public class HtmlParser {

  private final ObjectMapper mapper;

  public HtmlParser(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public FoodOrder parserOrder(String emailBody, FoodOrderSource source) throws JsonProcessingException {
    return switch (source) {
      case EFOOD -> parseEfoodOrder(emailBody);
      case WOLT -> parseWoltOrder(emailBody);
      case BOX -> null;
    };
  }

  private FoodOrder parseEfoodOrder(String emailBody) throws JsonProcessingException {
    Document document = Jsoup.parse(emailBody);
    Elements scripts = document.getElementsByTag("script");
    Optional<String> orderJson = scripts.stream().map(s -> s.childNodes().stream().findFirst().get().toString()).findFirst();
    return orderJson.isEmpty() ? null : mapper.readValue(orderJson.get(), FoodOrder.class);
  }

  private FoodOrder parseWoltOrder(String emailBody) throws JsonProcessingException {
    Document document = Jsoup.parse(emailBody);
    //TODO: parse a wolt email receipt
    return null;
  }

}
