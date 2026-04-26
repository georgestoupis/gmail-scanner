package com.gmail.scanner.service.parser.food;

import static com.gmail.scanner.service.parser.ParserUtils.foundTotalPrice;
import static com.gmail.scanner.service.parser.ParserUtils.normalizePrice;
import static com.gmail.scanner.service.parser.ParserUtils.parseHtmlTdElements;

import com.gmail.scanner.service.model.FoodOrder;
import com.gmail.scanner.service.model.Source;
import com.gmail.scanner.service.parser.EmailData;
import com.gmail.scanner.service.parser.OrderParser;
import java.time.LocalDateTime;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WoltOrderParser implements OrderParser {

  private static final String WOLT_PRICE_LABEL = "EUR";

  @Override
  public FoodOrder parseOrder(EmailData emailData, Source source, LocalDateTime orderDateTime) {
    Document document = Jsoup.parse(emailData.html());
    List<String> texts = parseHtmlTdElements(document);
    FoodOrder foodOrder = new FoodOrder();
    for (int i = 0; i < texts.size() - 1; i++) {
      if (texts.get(i).contains(WOLT_PRICE_LABEL)) {
        //Case 1: Price in the same line
        var priceText = normalizePrice(texts.get(i), WOLT_PRICE_LABEL);
        if (foundTotalPrice(foodOrder, priceText)) {
          foodOrder.setPrice(priceText);
        }
        //Case 2: Price in the next line
        var nextLine = normalizePrice(texts.get(i + 1), WOLT_PRICE_LABEL);
        if (foundTotalPrice(foodOrder, nextLine)) {
          foodOrder.setPrice(nextLine);
        }
      }
    }
    foodOrder.setSource(source);
    foodOrder.setDate(orderDateTime);
    return foodOrder;
  }
}
