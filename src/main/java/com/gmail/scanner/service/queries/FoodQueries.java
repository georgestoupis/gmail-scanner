package com.gmail.scanner.service.queries;

import com.gmail.scanner.service.model.Source;
import java.util.Map;

public final class FoodQueries {

  public static final Map<Source, String> SOURCE_QUERIES_MAP = Map.of(
      Source.EFOOD, "from:noreply@e-food.gr {subject:\"Η παραγγελία σου\" subject:\"από το efood!\"}",
      Source.WOLT, "from:info@wolt.com {subject:\"Your order’s confirmed\" subject:\"Your order’s delivered\" subject:\"Η παραγγελία σου παραδόθηκε\"}",
      Source.BOX, "from:support@box.gr subject:\"Η παραγγελία σας στο κατάστημα\"");

  private FoodQueries() {
  }
}
