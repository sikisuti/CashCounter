package org.siki.cashcounter.view.chart;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import org.siki.cashcounter.service.ChartService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

public class CashFlowChart extends LineChart<LocalDate, Number> {

  @Autowired ChartService chartService;

  public CashFlowChart(ChartService chartService) {
    super(
        new DateAxis(LocalDate.now().minusYears(1), LocalDate.now().plusYears(1)),
        new NumberAxis());
    this.setCreateSymbols(false);

    var series = chartService.getBalances();
    this.getData().add(series);
  }
}
