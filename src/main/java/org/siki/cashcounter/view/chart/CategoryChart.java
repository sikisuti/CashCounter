package org.siki.cashcounter.view.chart;

import static org.siki.cashcounter.service.CategoryService.RANGE;

import java.time.LocalDate;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import org.siki.cashcounter.service.CategoryService;

public class CategoryChart extends LineChart<LocalDate, Number> {
  CategoryService categoryService;

  @Override
  public NumberAxis getYAxis() {
    return (NumberAxis) super.getYAxis();
  }

  public CategoryChart(CategoryService categoryService) {
    super(new DateAxis(LocalDate.now().plusDays(RANGE), LocalDate.now()), new NumberAxis());
    this.categoryService = categoryService;

    getYAxis().setAutoRanging(false);
    setCreateSymbols(false);
  }

  public void refreshChart(String category, int weekRange) {
    var series = categoryService.getCategoryChartData(category, weekRange);
    setData(series);

    var mainSeries =
        series.stream().filter(s -> s.getName().contains(category)).findFirst().orElseThrow();

    var max =
        mainSeries.getData().stream()
            .mapToDouble(d -> d.getYValue().intValue())
            .max()
            .orElse(100000);
    var min =
        mainSeries.getData().stream().mapToDouble(d -> d.getYValue().intValue()).min().orElse(0);

    double diff = max - min;
    double unit;
    if (diff < 10000) {
      unit = 1000;
    } else if (diff < 100000) {
      unit = 10000;
    } else {
      unit = 100000;
    }

    getYAxis().setUpperBound(max > 0 ? Math.ceil(max / unit) * unit : 0);
    getYAxis().setLowerBound(min < 0 ? Math.floor(min / unit) * unit : 0);
    getYAxis().setTickUnit(unit);

    setTitle(category);
  }
}
