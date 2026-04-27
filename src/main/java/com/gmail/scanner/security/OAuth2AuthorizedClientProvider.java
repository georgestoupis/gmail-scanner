package com.gmail.scanner.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public record OAuth2AuthorizedClientProvider(OAuth2AuthorizedClientManager authorizedClientManager) {

  public OAuth2AuthorizedClient getClient() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
      throw new IllegalStateException("No OAuth2 authentication in security context");
    }
    OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
        .withClientRegistrationId(oauthToken.getAuthorizedClientRegistrationId())
        .principal(oauthToken)
        .build();
    return authorizedClientManager.authorize(request);
  }

}
