package com.gmail.scanner.model;

import com.fasterxml.jackson.annotation.JsonValue;
import com.gmail.scanner.exception.UnsupportedGroupException;
import com.gmail.scanner.service.model.Source;
import com.gmail.scanner.service.queries.FoodQueries;
import com.gmail.scanner.service.queries.GameQueries;
import com.gmail.scanner.service.queries.ShoppingQueries;
import com.gmail.scanner.service.queries.TravelQueries;
import java.util.Map;

public enum Group {
  FOOD(FoodQueries.SOURCE_QUERIES_MAP, "Some of it was worth it."),
  GAMES(GameQueries.SOURCE_QUERIES_MAP, "Did you play any of those?"),
  SHOPPING(ShoppingQueries.SOURCE_QUERIES_MAP, "Everything arrived, probably."),
  TRAVEL(TravelQueries.SOURCE_QUERIES_MAP, "You were somewhere else for a bit.");

  private final Map<Source, String> queries;
  public final String message;

  Group(Map<Source, String> queries, String message) {
    this.queries = queries;
    this.message = message;
  }

  public Map<Source, String> getQueries() {
    return queries;
  }

  public static Group from(String value) {
    try {
      return valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new UnsupportedGroupException("Unsupported group: " + value);
    }
  }

  @JsonValue
  public String value() {
    return name().toLowerCase();
  }
}
