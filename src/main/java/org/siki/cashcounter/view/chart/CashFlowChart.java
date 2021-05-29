package org.siki.cashcounter.view.chart;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import org.siki.cashcounter.service.ChartService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

public class CashFlowChart extends LineChart<LocalDate, Number> {

  @Autowired ChartService chartService;

  Series<LocalDate, Number> series = new Series<>();

  public CashFlowChart(ChartService chartService) {
    super(new DateAxis(), new NumberAxis());
    this.setCreateSymbols(false);

    this.getData().add(chartService.getBalances());
  }
}
