package com.gmail.scanner.service.queries;

import com.gmail.scanner.service.model.Source;
import java.util.Map;

public final class GameQueries {

  public static final Map<Source, String> SOURCE_QUERIES_MAP = Map.of(
      Source.RIOT, "from:noreply@mail.accounts.riotgames.com subject:\"Purchase Confirmation Receipt\"",
      Source.STEAM, "from:noreply@steampowered.com subject:\"Thank you for your Steam purchase\"",
      Source.KINGUIN, "(from:helpdesk@kinguin.net OR from:help@kinguin.net) subject:\"We have received your new order\"",
      Source.PLAYSTATION, "from:playstation.com subject:\"Thank You For Your Purchase\"",
      Source.NINTENDO, "from:no-reply@accounts.nintendo.com subject:\"Thank you for your Nintendo eShop purchase\"",
      Source.XBOX, "(from:microsoft-noreply@microsoft.com OR from:stremail@microsoft.com) (subject:\"Your Microsoft order\" OR subject:\"has been processed\" OR subject:\"Η παραγγελία-δώρο που κάνατε για\")");

  private GameQueries() {
  }
}
