package org.siki.cashcounter.view.dailycorrections;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import org.siki.cashcounter.model.MonthlyBalance;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.view.ViewFactory;

import java.time.YearMonth;

@SuppressWarnings("java:S110")
public class DailyCorrectionsView extends GridPane {
  private final DataManager dataManager;
  private final ViewFactory viewFactory;

  public final ObservableList<MonthlyBalanceTitledPane> monthlyBalanceTitledPanes =
      FXCollections.observableArrayList();

  public final VBox dailyBalancesPH = new VBox();

  public DailyCorrectionsView(DataManager dataManager, ViewFactory viewFactory) {
    this.dataManager = dataManager;
    this.viewFactory = viewFactory;

    Bindings.bindContent(dailyBalancesPH.getChildren(), monthlyBalanceTitledPanes);

    var columnConstraints = new ColumnConstraints();
    columnConstraints.setPercentWidth(100);
    this.getColumnConstraints().add(columnConstraints);
    var rowConstraints = new RowConstraints();
    rowConstraints.setVgrow(Priority.ALWAYS);
    this.getRowConstraints().add(rowConstraints);
    this.getChildren().add(new ScrollPane(dailyBalancesPH));

    loadCorrections();
  }

  private void loadCorrections() {
    dailyBalancesPH.getChildren().clear();
    dataManager.getMonthlyBalances().stream()
        .filter(mb -> mb.getYearMonth().isAfter(YearMonth.now().minusYears(1)))
        .forEach(mb -> monthlyBalanceTitledPanes.add(createMonthlyBalanceTitledPane(mb)));
  }

  private MonthlyBalanceTitledPane createMonthlyBalanceTitledPane(MonthlyBalance monthlyBalance) {
    var monthlyBalanceTitledPane =
        new MonthlyBalanceTitledPane(monthlyBalance, dataManager, viewFactory);
    monthlyBalanceTitledPane.expandedProperty().set(false);
    if (YearMonth.now().equals(monthlyBalance.getYearMonth())) {
      monthlyBalanceTitledPane.expandedProperty().set(true);
    }

    return monthlyBalanceTitledPane;
  }
}
