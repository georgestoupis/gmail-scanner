package com.gmail.scanner.service.queries;

import com.gmail.scanner.service.model.Source;
import java.util.Map;

public final class FoodQueries {

  public static final Map<Source, String> SOURCE_QUERIES_MAP = Map.of(
      Source.EFOOD, "from:noreply@e-food.gr AND (subject:Η παραγγελία σου OR subject:από το efood!)",
      Source.WOLT, "from:info@wolt.com AND subject:Your order’s confirmed",
      Source.BOX, "from:support@box.gr AND subject:Η παραγγελία σας στο κατάστημα");


  private FoodQueries() {
  }

}
