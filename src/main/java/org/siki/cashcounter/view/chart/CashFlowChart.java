package org.siki.cashcounter.view.chart;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;

import java.time.LocalDate;

public class CashFlowChart extends LineChart<LocalDate, Number> {
  Series<LocalDate, Number> series = new Series<>();

  public CashFlowChart(Series<LocalDate, Number> series) {
    super(
        new DateAxis(
            series.getData().get(0).getXValue(),
            series.getData().get(series.getData().size() - 1).getXValue()),
        new NumberAxis());
    series.setName("Dátum");

    this.getData().addAll(series);
    this.setCreateSymbols(false);
  }
}
