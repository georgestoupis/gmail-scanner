package com.gmail.scanner.service;

import com.gmail.scanner.google.GoogleServiceProvider;
import com.gmail.scanner.google.GoogleServiceType;
import com.gmail.scanner.security.OAuth2AuthorizedClientProvider;
import com.gmail.scanner.service.model.EfoodOrder;
import com.gmail.scanner.service.parser.HtmlParser;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EfoodService {

  private static final Logger LOG = LoggerFactory.getLogger(EfoodService.class);

  private final Gmail gmail;
  private final HtmlParser htmlParser;

  public EfoodService(GoogleServiceProvider googleServiceProvider, OAuth2AuthorizedClientProvider clientProvider, HtmlParser htmlParser)
      throws IOException, GeneralSecurityException {
    this.gmail = (Gmail) googleServiceProvider.getService(GoogleServiceType.GMAIL, clientProvider.getClient());
    this.htmlParser = htmlParser;
  }

  public List<EfoodOrder> getOrders(int year) throws IOException {

    //TODO: parameterize the below
    String user = "me";
    String query = "from:noreply@e-food.gr AND (subject:Η παραγγελία σου OR subject:από το efood!) AND before:" + year + "/12/31 AND after:" + year + "/01/01";
    Long maxResults = 10000L;

    // Read messages
    ListMessagesResponse listMessagesResponse = gmail.users().messages().list(user).setQ(query).setMaxResults(maxResults).execute();
    List<Message> messages = listMessagesResponse.getMessages();
    LOG.info("Got {} efood orders emails", messages.size());

    List<EfoodOrder> efoodOrders = new ArrayList<>();
    for (Message message : messages) {
      Message detailedMessage = gmail.users().messages().get(user, message.getId()).execute();
      byte[] data = detailedMessage.getPayload().getBody().decodeData();
      String dataString = new String(data, StandardCharsets.UTF_8);
      EfoodOrder efoodOrder = htmlParser.parseEfoodOrder(dataString);
      efoodOrders.add(efoodOrder);
    }

    return efoodOrders;
  }
}
