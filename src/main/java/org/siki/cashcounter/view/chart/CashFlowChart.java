package org.siki.cashcounter.view.chart;

import static javafx.scene.chart.XYChart.*;

public class CashFlowChart {
    private Series<Integer, Integer> series = new Series<>();

    public CashFlowChart() {
        for (int i = 0; i < 100; i++) {
            series.getData().add(new Data<>(i, i));
        }
    }
}
