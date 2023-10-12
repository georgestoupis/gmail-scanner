package com.gmail.scanner.service.queries;

import com.gmail.scanner.service.model.Source;
import java.util.Map;

public final class GameQueries {

  public static final Map<Source, String> SOURCE_QUERIES_MAP = Map.of(
      Source.RIOT, "from:noreply@mail.accounts.riotgames.com AND subject:Purchase Confirmation Receipt",
      Source.STEAM, "from:noreply@steampowered.com AND subject:Thank you for your Steam purchase");

  private GameQueries() {

  }

}
