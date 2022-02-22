package org.siki.cashcounter.view.chart;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import org.siki.cashcounter.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

public class CategoryChart extends LineChart<LocalDate, Number> {
  @Autowired CategoryService categoryService;

  @Override
  public NumberAxis getYAxis() {
    return (NumberAxis) super.getYAxis();
  }

  public CategoryChart(CategoryService categoryService) {
    super(new DateAxis(LocalDate.now().minusYears(2), LocalDate.now()), new NumberAxis());
    this.categoryService = categoryService;

    getYAxis().setAutoRanging(false);
    setCreateSymbols(false);
  }

  public void refreshChart(String category) {
    var series = categoryService.getCategoryChart(category);
    setData(series);

    var max =
        series.get(0).getData().stream()
            .mapToDouble(d -> d.getYValue().intValue())
            .max()
            .orElse(100000);
    var min =
        series.get(0).getData().stream().mapToDouble(d -> d.getYValue().intValue()).min().orElse(0);

    double diff = max - min;
    double unit;
    if (diff < 10000) {
      unit = 1000;
    } else if (diff < 100000) {
      unit = 10000;
    } else {
      unit = 100000;
    }

    getYAxis().setUpperBound(Math.ceil(max / unit) * unit);
    getYAxis().setLowerBound(Math.floor(min / unit) * unit);
    getYAxis().setTickUnit(unit);

    setTitle(category);
  }
}
