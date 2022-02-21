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

    setTitle("alskdflskjfl");

    getYAxis().setAutoRanging(false);
    getYAxis().setUpperBound(1000000.0);
    getYAxis().setTickUnit(100000);
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
    getYAxis().setUpperBound(Math.ceil(max / 100000d) * 100000);
    getYAxis().setLowerBound(0.0);
  }
}
