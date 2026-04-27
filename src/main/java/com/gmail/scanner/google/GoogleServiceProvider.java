package com.gmail.scanner.google;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Component;

@Component
public class GoogleServiceProvider {

  private static final String APPLICATION_NAME = "gmail-scanner";
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

  private final HttpTransport httpTransport;

  public GoogleServiceProvider() throws GeneralSecurityException, IOException {
    this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
  }

  public Gmail getGmailService(OAuth2AuthorizedClient client) {
    Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod());
    credential.setAccessToken(client.getAccessToken().getTokenValue());
    return new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
        .setApplicationName(APPLICATION_NAME)
        .build();
  }
}
