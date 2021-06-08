package org.siki.cashcounter.view;

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
import org.siki.cashcounter.view.model.ObservableMonthlyBalance;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class MonthlyBalanceTitledPane extends TitledPane {

  private final ObservableMonthlyBalance observableMonthlyBalance;

  private final VBox vbDailyBalances = new VBox();

  public MonthlyBalanceTitledPane(
      ObservableMonthlyBalance observableMonthlyBalance, ViewFactory viewFactory) {
    super(
        observableMonthlyBalance
            .getYearMonthProperty()
            .get()
            .format(DateTimeFormatter.ofPattern("yyyy.MMMM")),
        new GridPane());
    this.observableMonthlyBalance = observableMonthlyBalance;

    GridPane gpRoot = (GridPane) this.getContent();
    GridPane.setColumnIndex(vbDailyBalances, 0);
    gpRoot.getChildren().addAll(vbDailyBalances /*, gpStatisticsBg*/);
    this.expandedProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              if (Boolean.TRUE.equals(newValue) && vbDailyBalances.getChildren().isEmpty()) {
                observableMonthlyBalance
                    .getObservableDailyBalances()
                    .forEach(
                        odb ->
                            vbDailyBalances
                                .getChildren()
                                .add(viewFactory.createDailyBalanceControl(odb, this)));
              }
            });
    this.setExpanded(YearMonth.now().equals(observableMonthlyBalance.getYearMonthProperty().get()));
  }

  public void validate() {
    this.isValid();
  }

  private void isValid() {
    boolean isValid = observableMonthlyBalance.isValid();

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
