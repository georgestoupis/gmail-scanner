package com.gmail.scanner.service;

import com.gmail.scanner.google.GoogleServiceProvider;
import com.gmail.scanner.security.OAuth2AuthorizedClientProvider;
import com.gmail.scanner.service.model.Order;
import com.gmail.scanner.service.model.Source;
import com.gmail.scanner.service.parser.EmailData;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MimeTypeUtils;

public class OrderService {

  public static final String PERIOD_QUERY_STRING = " AND before:%1$d/12/31 AND after:%1$d/01/01";

  private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);
  private static final long GMAIL_MAX_RESULTS = 10000L;
  private static final int GMAIL_MESSAGE_BATCH_SIZE = 100;
  private static final String GMAIL_USER = "me";

  private final Gmail gmail;

  public OrderService(GoogleServiceProvider googleServiceProvider, OAuth2AuthorizedClientProvider clientProvider)
      throws IOException, GeneralSecurityException {
    this.gmail = (Gmail) googleServiceProvider.getService(clientProvider.getClient());
  }

  public Map<Source, List<Order>> getOrderMap(int year, Map<Source, String> queries) throws IOException {
    Map<Source, List<Order>> orders = new EnumMap<>(Source.class);
    for (Entry<Source, String> entry : queries.entrySet()) {
      orders.put(entry.getKey(), this.getOrdersFromSource(year, entry.getKey(), entry.getValue()));
    }
    return orders;
  }

  private List<Order> getOrdersFromSource(int year, Source source, String query) throws IOException {

    //Append period postfix to query
    query += PERIOD_QUERY_STRING;

    // Read messages
    ListMessagesResponse listMessagesResponse = gmail.users().messages()
        .list(GMAIL_USER)
        .setQ(query.formatted(year))
        .setMaxResults(GMAIL_MAX_RESULTS)
        .execute();
    List<Message> messages = listMessagesResponse.getMessages();
    if (messages == null) {
      LOG.info("No {} order emails found for {}", source, year);
      return Collections.emptyList();
    }
    LOG.info("Got {} {} order emails", messages.size(), source);

    List<Message> detailedMessageList = this.populateDetailedMessageList(messages);

    List<Order> orders = new ArrayList<>();
    for (Message detailedMessage : detailedMessageList) {
      byte[] data = detailedMessage.getPayload().getBody().decodeData();
      String payload = data == null ? null : new String(data, StandardCharsets.UTF_8);
      String plain = this.findMessagePartWithMimeType(detailedMessage.getPayload().getParts(), MimeTypeUtils.TEXT_PLAIN_VALUE);
      String html = this.findMessagePartWithMimeType(detailedMessage.getPayload().getParts(), MimeTypeUtils.TEXT_HTML_VALUE);
      EmailData emailData = new EmailData(payload, plain, html);

      Order order = source.getParser().parseOrder(emailData);
      if (order != null) {
        order.setSource(source);
        order.setDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(detailedMessage.getInternalDate()), ZoneId.systemDefault()));
        orders.add(order);
      }
    }
    LOG.info("Parsed {} {} orders", orders.size(), source);
    return orders;
  }

  private List<Message> populateDetailedMessageList(List<Message> messages) throws IOException {

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
        gmail.users().messages().get(GMAIL_USER, message.getId()).queue(batchRequest, callback);
      }
      batchRequest.execute();
    }

    return detailedMessageList;
  }

  private String findMessagePartWithMimeType(List<MessagePart> parts, String mimeType) {
    if (parts == null) {
      return null;
    }
    for (MessagePart part : parts) {
      if (mimeType.equalsIgnoreCase(part.getMimeType())) {
        return new String(part.getBody().decodeData(), StandardCharsets.UTF_8);
      }
      if (part.getParts() != null) {
        return findMessagePartWithMimeType(part.getParts(), mimeType);
      }
    }
    return null;
  }

}
