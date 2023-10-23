package com.gmail.scanner.service.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmail.scanner.service.parser.BoxOrderParser;
import com.gmail.scanner.service.parser.EfoodOrderParser;
import com.gmail.scanner.service.parser.KinguinOrderParser;
import com.gmail.scanner.service.parser.OrderParser;
import com.gmail.scanner.service.parser.PlaystationOrderParser;
import com.gmail.scanner.service.parser.RiotOrderParser;
import com.gmail.scanner.service.parser.SteamOrderParser;
import com.gmail.scanner.service.parser.WoltOrderParser;

public enum Source {
  EFOOD(new EfoodOrderParser(new ObjectMapper())),
  WOLT(new WoltOrderParser()),
  BOX(new BoxOrderParser()),
  STEAM(new SteamOrderParser()),
  KINGUIN(new KinguinOrderParser()),
  RIOT(new RiotOrderParser()),
  PLAYSTATION(new PlaystationOrderParser());

  final OrderParser parser;

  Source(OrderParser parser) {
    this.parser = parser;
  }

  public OrderParser getParser() {
    return parser;
  }
}
