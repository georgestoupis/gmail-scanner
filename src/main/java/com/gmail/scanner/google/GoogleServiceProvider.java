package com.gmail.scanner.google;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Component;

@Component
public class GoogleServiceProvider {

  /**
   * Application name.
   */
  private static final String APPLICATION_NAME = "gmail-scanner";
  /**
   * Global instance of the JSON factory.
   */
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

  public AbstractGoogleJsonClient getService(OAuth2AuthorizedClient client) throws GeneralSecurityException, IOException {

    return new Gmail.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, getCredentials(client)).setApplicationName(APPLICATION_NAME).build();
  }

  private Credential getCredentials(final OAuth2AuthorizedClient client) {

    Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod());
    credential.setAccessToken(client.getAccessToken().getTokenValue());
    return credential;
  }
}
