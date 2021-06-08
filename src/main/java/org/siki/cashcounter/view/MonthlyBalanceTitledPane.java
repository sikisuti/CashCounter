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

  private ObservableMonthlyBalance observableMonthlyBalance;
  private ViewFactory viewFactory;

  //    private YearMonth period;
  //    private MonthlyBalance // TODO
  //    private ObservableList<DailyBalance> dailyBalances = FXCollections.observableArrayList();
  private VBox vbDailyBalances = new VBox();

  //    public LocalDate getPeriod() { return period; }

  /*public void addDailyBalance(DailyBalance db) {
      dailyBalances.add(db);
      if (isAroundToday(period))
          vbDailyBalances.getChildren().add(new DailyBalanceControl(db, this));
  }*/

  //    public List<DailyBalance> getDailyBalances() {
  //        return dailyBalances;
  //    }

  public MonthlyBalanceTitledPane(
      ObservableMonthlyBalance observableMonthlyBalance, ViewFactory viewFactory) {
    super(
        observableMonthlyBalance
            .getYearMonthProperty()
            .get()
            .format(DateTimeFormatter.ofPattern("yyyy.MMMM")),
        new GridPane());
    this.observableMonthlyBalance = observableMonthlyBalance;
    this.viewFactory = viewFactory;

    GridPane gpRoot = (GridPane) this.getContent();
    GridPane.setColumnIndex(vbDailyBalances, 0);
    gpRoot.getChildren().addAll(vbDailyBalances /*, gpStatisticsBg*/);
    this.setExpanded(YearMonth.now().equals(observableMonthlyBalance.getYearMonthProperty()));
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
