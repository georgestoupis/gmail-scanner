package com.gmail.scanner.service;

import com.gmail.scanner.exception.InsufficientScopeException;
import com.gmail.scanner.service.model.Order;
import com.gmail.scanner.service.model.Source;
import com.gmail.scanner.service.parser.EmailData;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

@Service
public class OrderService {

  private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);
  private static final long GMAIL_PAGE_MAX_RESULTS = 500L;
  private static final int GMAIL_MESSAGE_BATCH_SIZE = 20;
  private static final String GMAIL_USER = "me";
  private static final String PERIOD_QUERY_STRING = " AND before:%1$d/12/31 AND after:%1$d/01/01";

  public Map<Source, List<Order>> getOrderMap(Gmail gmail, int year, Map<Source, String> queries) {
    Map<Source, List<Order>> orders = new EnumMap<>(Source.class);
    try {
      for (Entry<Source, String> entry : queries.entrySet()) {
        orders.put(entry.getKey(), this.getOrdersFromSource(gmail, year, entry.getKey(), entry.getValue()));
      }
      return orders;
    } catch (GoogleJsonResponseException ex) {
      if (ex.getStatusCode() == 403) {
        throw new InsufficientScopeException(ex.getMessage());
      }
      throw new RuntimeException(ex);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private List<Order> getOrdersFromSource(Gmail gmail, int year, Source source, String query) throws IOException {
    query += PERIOD_QUERY_STRING.formatted(year);
    List<Message> messages = this.loadMessagesPaginated(gmail, query);
    if (messages.isEmpty()) {
      LOG.info("No {} order emails found for {}", source, year);
      return Collections.emptyList();
    }
    LOG.info("Got {} {} order emails for {}", messages.size(), source, year);

    List<Message> detailedMessageList = this.populateDetailedMessageList(gmail, messages);

    List<Order> orders = new ArrayList<>();
    for (Message detailedMessage : detailedMessageList) {
      byte[] data = detailedMessage.getPayload().getBody().decodeData();
      String payload = data == null ? null : new String(data, StandardCharsets.UTF_8);
      String plain = this.findMessagePartWithMimeType(detailedMessage.getPayload().getParts(), MimeTypeUtils.TEXT_PLAIN_VALUE);
      String html = this.findMessagePartWithMimeType(detailedMessage.getPayload().getParts(), MimeTypeUtils.TEXT_HTML_VALUE);
      EmailData emailData = new EmailData(payload, plain, html);

      LocalDateTime orderDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(detailedMessage.getInternalDate()), ZoneId.systemDefault());
      Optional<String> price = source.getParser().parseOrderPrice(emailData);
      if (price.isPresent()) {
        orders.add(new Order(source, orderDateTime, price.get()));
      } else {
        LOG.warn("{} {} Failed to parse email messageId={}", source, year, detailedMessage.getId());
        LOG.debug("{} {} Failed to parse email body: {}", source, year, this.logBase64(emailData.toString()));
      }
    }
    LOG.info("Parsed {} {} orders for {}", orders.size(), source, year);
    return orders;
  }

  private List<Message> loadMessagesPaginated(Gmail gmail, String query) throws IOException {
    List<Message> messages = new ArrayList<>();
    String pageToken = null;
    do {
      ListMessagesResponse resp = gmail.users().messages()
          .list(GMAIL_USER)
          .setQ(query)
          .setMaxResults(GMAIL_PAGE_MAX_RESULTS)
          .setPageToken(pageToken)
          .execute();
      if (resp.getMessages() != null) {
        messages.addAll(resp.getMessages());
      }
      pageToken = resp.getNextPageToken();
    } while (pageToken != null);
    return messages;
  }

  private List<Message> populateDetailedMessageList(Gmail gmail, List<Message> messages) throws IOException {
    List<Message> detailedMessageList = new ArrayList<>();

    final JsonBatchCallback<Message> callback = new JsonBatchCallback<>() {
      public void onSuccess(Message message, HttpHeaders responseHeaders) {
        detailedMessageList.add(message);
      }

      public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
        LOG.warn("Couldn't get detailed message: {}", e.getMessage());
      }
    };

    BatchRequest batchRequest = gmail.batch();
    List<List<Message>> lists = Lists.partition(messages, GMAIL_MESSAGE_BATCH_SIZE);
    for (List<Message> list : lists) {
      for (Message message : list) {
        gmail.users().messages().get(GMAIL_USER, message.getId()).queue(batchRequest, callback);
      }
      LOG.debug("Sending GMAIL API request (batch of: {})", list.size());
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
        String result = findMessagePartWithMimeType(part.getParts(), mimeType);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  private String logBase64(String string) {
    return Base64.getEncoder().encodeToString(shrinkString(string).getBytes(StandardCharsets.UTF_8));
  }

  private static String shrinkString(String string) {
    return string == null
        ? null
        : string.lines().map(String::trim).filter(x -> !x.isBlank()).reduce("", String::concat);
  }
}
