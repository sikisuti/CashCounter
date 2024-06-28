package org.siki.cashcounter.view.dialog;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.service.CategoryService;
import org.siki.cashcounter.service.CorrectionService;
import org.siki.cashcounter.view.chart.CategoryChartGrid;

public class CategoryChartDialog extends Stage {
  private final CategoryChartGrid categoryChartGrid;

  public CategoryChartDialog(
      CategoryService categoryService,
      ConfigurationManager configurationManager,
      CorrectionService correctionService) {
    this.categoryChartGrid =
        new CategoryChartGrid(categoryService, configurationManager, correctionService);
    setScene(new Scene(categoryChartGrid));
    this.initStyle(StageStyle.DECORATED);

    setWidth(1300);
    setHeight(700);
  }

  public void showChart(String category) {
    categoryChartGrid.setCategory(category);
    categoryChartGrid.refresh();
    showAndWait();
  }
}
