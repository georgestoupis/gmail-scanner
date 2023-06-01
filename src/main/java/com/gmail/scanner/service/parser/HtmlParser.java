package com.gmail.scanner.service.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmail.scanner.service.model.FoodOrder;
import com.gmail.scanner.service.model.FoodOrderSource;
import com.google.common.base.CharMatcher;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
      case BOX -> parseBoxOrder(emailBody);
    };
  }

  private FoodOrder parseEfoodOrder(String emailBody) throws JsonProcessingException {
    Document document = Jsoup.parse(emailBody);
    Elements scripts = document.getElementsByTag("script");
    Optional<String> orderJson = scripts.stream().map(s -> s.childNodes().stream().findFirst().get().toString()).findFirst();
    return orderJson.isEmpty() ? null : mapper.readValue(orderJson.get(), FoodOrder.class);
  }

  private FoodOrder parseWoltOrder(String emailBody) {
    Document document = Jsoup.parse(emailBody);
    List<String> texts = document.getElementsByTag("td").eachText().stream().map(s -> CharMatcher.ascii().retainFrom(s)).toList();
    FoodOrder foodOrder = new FoodOrder();
    //Yes this is flaky af, no I don't currently care
    for (int i = 0; i < texts.size(); i++) {
      if (texts.get(i).contains("Total in EUR")) {
        if (foodOrder.getPrice() == null || Double.parseDouble(foodOrder.getPrice()) < Double.parseDouble(texts.get(i + 1))) {
          foodOrder.setPrice(texts.get(i + 1));
        }
      } else if (texts.get(i).equals("Delivery time")) {
        String date = texts.get(i + 1);
        try {
          foodOrder.setOrderDate(LocalDateTime.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        } catch (Exception ignored) {

        }
      }
      if (foodOrder.getPrice() != null && foodOrder.getOrderDate() != null) {
        break;
      }
    }
    return foodOrder;
  }

  private FoodOrder parseBoxOrder(String emailBody) {
    Document document = Jsoup.parse(emailBody);
    List<String> texts = document.getElementsByTag("td").eachText();
    FoodOrder foodOrder = new FoodOrder();
    //Yes this is flaky af, no I don't currently care
    for (int i = 0; i < texts.size(); i++) {
      if (texts.get(i).equals("Τελικό ποσό")) {
        String price = texts.get(i + 1);
        foodOrder.setPrice(price.replace(",", ".").replace(" €", ""));
      } else if (texts.get(i).equals("Ημερομηνία / Ώρα")) {
        String date = texts.get(i + 1);
        foodOrder.setOrderDate(LocalDateTime.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm")));
      }
      if (foodOrder.getPrice() != null && foodOrder.getOrderDate() != null) {
        break;
      }
    }
    return foodOrder;
  }
}
