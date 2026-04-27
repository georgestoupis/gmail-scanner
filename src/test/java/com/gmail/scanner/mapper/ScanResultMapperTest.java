package com.gmail.scanner.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.gmail.scanner.model.ScanResult;
import com.gmail.scanner.service.model.Order;
import com.gmail.scanner.service.model.Source;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ScanResultMapperTest {

  private final ScanResultMapper mapper = new ScanResultMapper();

  @Test
  void fromOrderMap_dividesByTwelveForPastYear() {
    Order order = new Order(Source.EFOOD, LocalDateTime.now(), "120.00");
    ScanResult result = mapper.fromOrderMap("food", 2023, Map.of(Source.EFOOD, List.of(order)));
    assertThat(result.avgMonth()).isEqualByComparingTo("10.00");
  }

  @Test
  void fromOrderMap_dividesByCurrentMonthForCurrentYear() {
    Order order = new Order(Source.EFOOD, LocalDateTime.now(), "120.00");
    int currentYear = LocalDate.now().getYear();
    int currentMonth = LocalDate.now().getMonthValue();
    ScanResult result = mapper.fromOrderMap("food", currentYear, Map.of(Source.EFOOD, List.of(order)));
    BigDecimal expected = new BigDecimal("120.00").divide(BigDecimal.valueOf(currentMonth), RoundingMode.HALF_UP);
    assertThat(result.avgMonth()).isEqualByComparingTo(expected);
  }

  @Test
  void fromOrderMap_appliesHalfUpRounding() {
    Order order = new Order(Source.EFOOD, LocalDateTime.now(), "10.00");
    ScanResult result = mapper.fromOrderMap("food", 2023, Map.of(Source.EFOOD, List.of(order)));
    assertThat(result.avgMonth()).isEqualByComparingTo("0.83"); // 10.00 / 12 = 0.8333... → 0.83
  }

  @Test
  void fromOrderMap_aggregatesTotalAcrossSources() {
    Order efoodOrder = new Order(Source.EFOOD, LocalDateTime.now(), "12.50");
    Order woltOrder = new Order(Source.WOLT, LocalDateTime.now(), "7.50");
    ScanResult result = mapper.fromOrderMap("food", 2023, Map.of(
        Source.EFOOD, List.of(efoodOrder),
        Source.WOLT, List.of(woltOrder)
    ));
    assertThat(result.totalSum()).isEqualByComparingTo("20.00");
  }

  @Test
  void fromOrderMap_handlesEmptyOrderListForSource() {
    ScanResult result = mapper.fromOrderMap("food", 2023, Map.of(Source.EFOOD, List.of()));
    assertThat(result.totalSum()).isEqualByComparingTo("0");
    assertThat(result.sources()).hasSize(1);
    assertThat(result.sources().get(0).orders()).isEqualTo(0);
  }

  @Test
  void fromOrderMap_countsOrdersAndSumsPerSource() {
    Order o1 = new Order(Source.EFOOD, LocalDateTime.now(), "12.50");
    Order o2 = new Order(Source.EFOOD, LocalDateTime.now(), "8.00");
    ScanResult result = mapper.fromOrderMap("food", 2023, Map.of(Source.EFOOD, List.of(o1, o2)));
    assertThat(result.sources().get(0).orders()).isEqualTo(2);
    assertThat(result.sources().get(0).sum()).isEqualByComparingTo("20.50");
  }

  @Test
  void fromOrderMap_setsGroupAndPeriod() {
    ScanResult result = mapper.fromOrderMap("games", 2024, Map.of(Source.STEAM, List.of()));
    assertThat(result.group()).isEqualTo("games");
    assertThat(result.period()).isEqualTo("2024");
  }
}
