package org.siki.cashcounter.view.chart;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.input.ScrollEvent;
import javafx.scene.shape.Line;
import org.siki.cashcounter.service.ChartService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

public class CashFlowChart extends LineChart<LocalDate, Number> {

  @Autowired ChartService chartService;

  private Double maxUpperBound;
  private Double yDistance;

  Data<LocalDate, Integer> nowLine;

  @Override
  public NumberAxis getYAxis() {
    return (NumberAxis) super.getYAxis();
  }

  public CashFlowChart(ChartService chartService) {
    super(
        new DateAxis(LocalDate.now().minusYears(1), LocalDate.now().plusYears(1)),
        new NumberAxis());
    this.chartService = chartService;

    nowLine = new Data<>(LocalDate.now(), 0);
    var line1 = new Line();
    nowLine.setNode(line1);
    getPlotChildren().add(line1);

    getYAxis().setAutoRanging(false);
    getYAxis().setTickUnit(100000);
    setCreateSymbols(false);
    setPrefHeight(900);
    setOnScroll(this::scrollChart);
  }

  public void refreshChart() {
    var series = chartService.getSeries();
    setData(series);

    int max =
        series.get(1).getData().stream().mapToInt(s -> s.getYValue().intValue()).max().orElse(0);
    int min =
        series.get(1).getData().stream()
            .filter(s -> s.getXValue().isAfter(LocalDate.now()))
            .mapToInt(s -> s.getYValue().intValue())
            .min()
            .orElse(0);

    maxUpperBound = Math.ceil(max / 100000d) * 100000;
    double lowerBound = Math.floor((min - 350000) / 100000d) * 100000;
    double upperBound = lowerBound + 2000000;
    yDistance = upperBound - lowerBound;

    getYAxis().setUpperBound(upperBound);
    getYAxis().setLowerBound(lowerBound);
  }

  private void scrollChart(ScrollEvent event) {
    var amount = event.getDeltaY() * 10000;
    double min = getYAxis().getLowerBound();
    double max = getYAxis().getUpperBound();
    if (amount < 0) {
      min += amount;
      if (min < 0) {
        min = 0;
      }

      max = min + yDistance;
    } else {
      max += amount;
      if (max > maxUpperBound) {
        max = maxUpperBound;
      }

      min = max - yDistance;
    }

    getYAxis().setLowerBound(min);
    getYAxis().setUpperBound(max);
  }

  @Override
  protected void layoutPlotChildren() {
    super.layoutPlotChildren();

    var line1 = (Line) nowLine.getNode();
    line1.setStartX(getXAxis().getDisplayPosition(nowLine.getXValue()));
    line1.setEndX(line1.getStartX());
    line1.setStartY(0d);
    line1.setEndY(getBoundsInLocal().getHeight());
    line1.toFront();
  }
}
