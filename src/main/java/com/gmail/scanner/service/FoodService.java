package com.gmail.scanner.service;

import com.gmail.scanner.google.GoogleServiceProvider;
import com.gmail.scanner.google.GoogleServiceType;
import com.gmail.scanner.security.OAuth2AuthorizedClientProvider;
import com.gmail.scanner.service.model.FoodOrder;
import com.gmail.scanner.service.model.FoodOrderSource;
import com.gmail.scanner.service.parser.HtmlParser;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FoodService {

  private static final Logger LOG = LoggerFactory.getLogger(FoodService.class);
  private static final int GMAIL_MESSAGE_BATCH_SIZE = 100;
  private static final Map<FoodOrderSource, String> queries = Map.of(
      FoodOrderSource.EFOOD, "from:noreply@e-food.gr AND (subject:Η παραγγελία σου OR subject:από το efood!) AND before:%1$d/12/31 AND after:%1$d/01/01",
      FoodOrderSource.WOLT, "from:info@wolt.com AND subject:Your order’s confirmed AND before:%1$d/12/31 AND after:%1$d/01/01",
      FoodOrderSource.BOX, "from:support@box.gr AND subject:Η παραγγελία σας στο κατάστημα AND before:%1$d/12/31 AND after:%1$d/01/01\"");

  private final Gmail gmail;
  private final HtmlParser htmlParser;

  public FoodService(GoogleServiceProvider googleServiceProvider, OAuth2AuthorizedClientProvider clientProvider, HtmlParser htmlParser)
      throws IOException, GeneralSecurityException {
    this.gmail = (Gmail) googleServiceProvider.getService(GoogleServiceType.GMAIL, clientProvider.getClient());
    this.htmlParser = htmlParser;
  }

  public List<FoodOrder> getOrders(int year, FoodOrderSource source) throws IOException {

    String user = "me";
    Long maxResults = 10000L;

    // Read messages
    ListMessagesResponse listMessagesResponse = gmail.users().messages().list(user)
        .setQ(queries.get(source).formatted(year))
        .setMaxResults(maxResults)
        .execute();
    List<Message> messages = listMessagesResponse.getMessages();
    if (messages == null) {
      LOG.info("No {} order emails found for {}", source, year);
      return Collections.emptyList();
    }
    LOG.info("Got {} {} order emails", messages.size(), source);

    List<Message> detailedMessageList = this.populateDetailedMessageList(messages, user);

    List<FoodOrder> foodOrders = new ArrayList<>();
    for (Message detailedMessage : detailedMessageList) {
      byte[] data = detailedMessage.getPayload().getBody().decodeData();
      if (data == null) {
        data = detailedMessage.getPayload().getParts().get(0).getBody().decodeData();
      }
      String dataString = new String(data, StandardCharsets.UTF_8);
      FoodOrder foodOrder = htmlParser.parserOrder(dataString, source);
      if (foodOrder != null) {
        foodOrders.add(foodOrder);
      }
    }
    LOG.info("Parsed {} {} orders", foodOrders.size(), source);
    return foodOrders;
  }

  private List<Message> populateDetailedMessageList(List<Message> messages, String user) throws IOException {

    List<Message> detailedMessageList = new ArrayList<>();

    //A callback for the batch request that adds a detailed msg to the list
    final JsonBatchCallback<Message> callback = new JsonBatchCallback<Message>() {
      public void onSuccess(Message message, HttpHeaders responseHeaders) {
        detailedMessageList.add(message);
      }

      public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
        // do what you want if error occurs
        LOG.warn("Couldn't get detailed message: {}", e.getMessage());
      }
    };

    //Request detailed messages in batches
    BatchRequest batchRequest = gmail.batch();
    List<List<Message>> lists = Lists.partition(messages, GMAIL_MESSAGE_BATCH_SIZE);
    for (List<Message> list : lists) {
      for (Message message : list) {
        gmail.users().messages().get(user, message.getId()).queue(batchRequest, callback);
      }
      batchRequest.execute();
    }

    return detailedMessageList;
  }

}
