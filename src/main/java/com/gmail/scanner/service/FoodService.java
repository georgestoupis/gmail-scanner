package com.gmail.scanner.service;

import com.gmail.scanner.google.GoogleServiceProvider;
import com.gmail.scanner.security.OAuth2AuthorizedClientProvider;
import com.gmail.scanner.service.model.FoodOrder;
import com.gmail.scanner.service.model.Order;
import com.gmail.scanner.service.model.Source;
import com.gmail.scanner.service.parser.HtmlParser;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FoodService extends OrderService {

  private static final Logger LOG = LoggerFactory.getLogger(FoodService.class);

  private static final Map<Source, String> queries = Map.of(
      Source.EFOOD, "from:noreply@e-food.gr AND (subject:Η παραγγελία σου OR subject:από το efood!) AND before:%1$d/12/31 AND after:%1$d/01/01",
      Source.WOLT, "from:info@wolt.com AND subject:Your order’s confirmed AND before:%1$d/12/31 AND after:%1$d/01/01",
      Source.BOX, "from:support@box.gr AND subject:Η παραγγελία σας στο κατάστημα AND before:%1$d/12/31 AND after:%1$d/01/01\"");

  public FoodService(GoogleServiceProvider googleServiceProvider, OAuth2AuthorizedClientProvider clientProvider, HtmlParser htmlParser)
      throws IOException, GeneralSecurityException {
    super(googleServiceProvider, clientProvider, htmlParser);
  }

  public List<Order> getAllOrders(int year) throws IOException {
    List<Order> allOrders = new ArrayList<>();
    for (Entry<Source, String> entry : queries.entrySet()) {
      allOrders.addAll(this.getOrdersFromSource(year, entry.getKey(), entry.getValue()).stream().map(o -> (FoodOrder) o).toList());
    }
    return allOrders;
  }

}
