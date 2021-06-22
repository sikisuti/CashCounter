package org.siki.cashcounter.view;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.siki.cashcounter.model.MonthlyBalance;

import java.time.format.DateTimeFormatter;

public class MonthlyBalanceTitledPane extends TitledPane {
  private final MonthlyBalance monthlyBalance;
  private final ViewFactory viewFactory;

  private final ObservableList<DailyBalanceControl> dailyBalanceControls;

  private final VBox vbDailyBalances = new VBox();

  public MonthlyBalanceTitledPane(MonthlyBalance monthlyBalance, ViewFactory viewFactory) {
    super(
        monthlyBalance.getYearMonth().format(DateTimeFormatter.ofPattern("yyyy.MMMM")),
        new GridPane());
    this.monthlyBalance = monthlyBalance;
    this.viewFactory = viewFactory;

    dailyBalanceControls = FXCollections.observableArrayList();

    loadUI();
  }

  private void loadUI() {
    GridPane gpRoot = (GridPane) this.getContent();
    GridPane.setColumnIndex(vbDailyBalances, 0);
    gpRoot.getChildren().addAll(vbDailyBalances /*, gpStatisticsBg*/);
    Bindings.bindContent(vbDailyBalances.getChildren(), dailyBalanceControls);
    this.expandedProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              if (Boolean.TRUE.equals(newValue) && dailyBalanceControls.isEmpty()) {
                monthlyBalance
                    .getDailyBalances()
                    .forEach(
                        db ->
                            dailyBalanceControls.add(
                                viewFactory.createDailyBalanceControl(db, this)));
              }
            });
  }

  public void validate() {
    this.isValid();
  }

  private void isValid() {
    boolean isValid = monthlyBalance.isValid();

    if (!isValid) {
      this.setBorder(
          new Border(
              new BorderStroke(
                  Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    }

    if (this.isExpanded()) {
      for (Node child : vbDailyBalances.getChildren()) {
        if (child.getClass() == DailyBalanceControl.class) {
          ((DailyBalanceControl) child).isValid();
        }
      }
    }
  }
}
