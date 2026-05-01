package com.gmail.scanner.service.queries;

import com.gmail.scanner.service.model.Source;
import java.util.Map;

public final class TravelQueries {

  public static final Map<Source, String> SOURCE_QUERIES_MAP = Map.of(
      Source.UBER, "from:noreply@uber.com subject:\"trip with Uber\""
  );

  private TravelQueries() {
  }
}
