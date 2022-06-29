package org.siki.cashcounter.view.cashflow;

import javafx.geometry.Pos;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import org.siki.cashcounter.view.Refreshable;

@SuppressWarnings("java:S110")
public class CashFlowView extends VBox implements Refreshable {
  public final CashFlowChart cashFlowChart;

  public CashFlowView(CashFlowChart cashFlowChart) {
    this.cashFlowChart = cashFlowChart;

    getChildren().add(cashFlowChart);

    setAlignment(Pos.CENTER);
    setOnScroll(this::scrollChart);
  }

  private void scrollChart(ScrollEvent scrollEvent) {
    // To be implemented
  }

  @Override
  public void refresh() {
    cashFlowChart.refreshChart();
  }
}
