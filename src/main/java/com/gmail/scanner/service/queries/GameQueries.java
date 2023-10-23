package com.gmail.scanner.service.queries;

import com.gmail.scanner.service.model.Source;
import java.util.Map;

public final class GameQueries {

  public static final Map<Source, String> SOURCE_QUERIES_MAP = Map.of(
      Source.RIOT, "from:noreply@mail.accounts.riotgames.com AND subject:Purchase Confirmation Receipt",
      Source.STEAM, "from:noreply@steampowered.com AND subject:\"Thank you for your Steam purchase\"",
      Source.KINGUIN, "(from:helpdesk@kinguin.net OR from:help@kinguin.net) AND subject:We have received your new order",
      Source.PLAYSTATION, "from:playstation.com AND subject:Thank You For Your Purchase");

  private GameQueries() {
  }
}
