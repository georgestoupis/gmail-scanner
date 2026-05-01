package com.gmail.scanner.service;

import com.gmail.scanner.config.AppConfiguration;
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
  private static final String GMAIL_USER = "me";
  private static final String PERIOD_QUERY_STRING = " AND before:%1$d/12/31 AND after:%1$d/01/01";
  private static final String ALL_TIME_QUERY_STRING = " AND after:%1$d/01/01";

  private final AppConfiguration appConfig;

  public OrderService(AppConfiguration appConfig) {
    this.appConfig = appConfig;
  }

  public Map<Source, List<Order>> getOrderMapAllTime(Gmail gmail, Map<Source, String> queries) {
    Map<Source, List<Order>> orders = new EnumMap<>(Source.class);
    try {
      for (Entry<Source, String> entry : queries.entrySet()) {
        String query = entry.getValue() + ALL_TIME_QUERY_STRING.formatted(appConfig.startYear());
        orders.put(entry.getKey(), this.fetchOrdersForQuery(gmail, entry.getKey(), query));
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

  public Map<Source, List<Order>> getOrderMapForYear(Gmail gmail, Map<Source, String> queries, int year) {
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
    return fetchOrdersForQuery(gmail, source, query + PERIOD_QUERY_STRING.formatted(year));
  }

  private List<Order> fetchOrdersForQuery(Gmail gmail, Source source, String query) throws IOException {
    List<Message> messages = this.loadMessagesPaginated(gmail, query);
    if (messages.isEmpty()) {
      LOG.info("No {} order emails found", source);
      return Collections.emptyList();
    }
    LOG.info("Got {} {} order emails", messages.size(), source);

    List<Message> detailedMessageList = this.populateDetailedMessageList(gmail, messages);

    List<Order> orders = new ArrayList<>();
    for (Message detailedMessage : detailedMessageList) {
      byte[] data = detailedMessage.getPayload().getBody().decodeData();
      String payload = data == null ? null : new String(data, StandardCharsets.UTF_8);
      String plain = this.findMessagePartWithMimeType(detailedMessage.getPayload().getParts(), MimeTypeUtils.TEXT_PLAIN_VALUE);
      String html = this.findMessagePartWithMimeType(detailedMessage.getPayload().getParts(), MimeTypeUtils.TEXT_HTML_VALUE);
      EmailData emailData = new EmailData(payload, plain, html);
      LocalDateTime orderDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(detailedMessage.getInternalDate()), ZoneId.systemDefault());
      Optional<Order> order = createOrder(source, detailedMessage.getId(), orderDateTime, emailData);
      order.ifPresent(orders::add);
    }
    LOG.info("Parsed {} {} orders", orders.size(), source);
    return orders;
  }

  private List<Message> loadMessagesPaginated(Gmail gmail, String query) throws IOException {
    List<Message> messages = new ArrayList<>();
    String pageToken = null;
    do {
      ListMessagesResponse resp = gmail.users().messages()
          .list(GMAIL_USER)
          .setQ(query)
          .setMaxResults((long) appConfig.gmail().pageMaxResults())
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
    List<List<Message>> lists = Lists.partition(messages, appConfig.gmail().batchSize());
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

  private Optional<Order> createOrder(Source source, String detailedMessageId, LocalDateTime orderDateTime, EmailData emailData) {
    try {
      Optional<String> price = source.getParser().parseOrderPrice(emailData);
      if (price.isPresent()) {
        return Optional.of(new Order(source, orderDateTime, price.get()));
      }
    } catch (Exception ignored) {
    }
    LOG.warn("{} {} Failed to parse email messageId={}", source, orderDateTime.getYear(), detailedMessageId);
    LOG.debug("{} {} Failed to parse email body: {}", source, orderDateTime.getYear(), this.logBase64(emailData.toString()));
    return Optional.empty();
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
