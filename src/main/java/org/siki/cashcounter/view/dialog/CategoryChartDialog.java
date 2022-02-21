package org.siki.cashcounter.view.dialog;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.siki.cashcounter.service.CategoryService;
import org.siki.cashcounter.view.chart.CategoryChart;

public class CategoryChartDialog extends Stage {
  private final CategoryService categoryService;
  private final CategoryChart categoryChart;

  public CategoryChartDialog(CategoryService categoryService) {
    this.categoryService = categoryService;
    categoryChart = new CategoryChart(categoryService);
    loadUI();
  }

  public void show(String category) {
    categoryChart.refreshChart(category);
    showAndWait();
  }

  private void loadUI() {
    setScene(new Scene(new StackPane(categoryChart)));
    this.initStyle(StageStyle.UTILITY);
  }
}
