package com.gmail.scanner.service.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmail.scanner.service.parser.food.BoxOrderParser;
import com.gmail.scanner.service.parser.food.EfoodOrderParser;
import com.gmail.scanner.service.parser.games.KinguinOrderParser;
import com.gmail.scanner.service.parser.OrderParser;
import com.gmail.scanner.service.parser.games.PlaystationOrderParser;
import com.gmail.scanner.service.parser.games.RiotOrderParser;
import com.gmail.scanner.service.parser.shopping.SkroutzOrderParser;
import com.gmail.scanner.service.parser.games.SteamOrderParser;
import com.gmail.scanner.service.parser.travel.UberOrderParser;
import com.gmail.scanner.service.parser.food.WoltOrderParser;

public enum Source {
  EFOOD(new EfoodOrderParser(new ObjectMapper())),
  WOLT(new WoltOrderParser()),
  BOX(new BoxOrderParser()),
  STEAM(new SteamOrderParser()),
  KINGUIN(new KinguinOrderParser()),
  RIOT(new RiotOrderParser()),
  PLAYSTATION(new PlaystationOrderParser()),
  SKROUTZ(new SkroutzOrderParser()),
  UBER(new UberOrderParser());

  final OrderParser parser;

  Source(OrderParser parser) {
    this.parser = parser;
  }

  public OrderParser getParser() {
    return parser;
  }
}
