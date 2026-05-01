package com.gmail.scanner.service.queries;

import com.gmail.scanner.service.model.Source;
import java.util.Map;

public final class ShoppingQueries {

  public static final Map<Source, String> SOURCE_QUERIES_MAP = Map.of(
      Source.SKROUTZ, "from:ecommerce-support@skroutz.gr {subject:\"λάβαμε την παραγγελία σου\" subject:\"παραγγελία σου καταχωρήθηκε\"}"
  );

  private ShoppingQueries() {
  }
}
