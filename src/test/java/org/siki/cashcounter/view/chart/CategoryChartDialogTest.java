package org.siki.cashcounter.view.chart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.siki.cashcounter.view.chart.CategoryChartGrid.Range.*;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CategoryChartDialogTest {
  @ParameterizedTest
  @MethodSource("weekStream")
  void rangeFromWeed(int weeks, CategoryChartGrid.Range expectedRange) {
    assertThat(CategoryChartGrid.Range.fromWeeks(weeks)).isEqualTo(expectedRange);
  }

  public static Stream<Arguments> weekStream() {
    return Stream.of(
        Arguments.of(4, ONE_MONTH),
        Arguments.of(13, QUARTER),
        Arguments.of(26, HALF_YEAR),
        Arguments.of(52, ONE_YEAR),
        Arguments.of(104, TWO_YEARS),
        Arguments.of(10, QUARTER),
        Arguments.of(1, ONE_MONTH),
        Arguments.of(200, TWO_YEARS),
        Arguments.of(60, ONE_YEAR));
  }
}
