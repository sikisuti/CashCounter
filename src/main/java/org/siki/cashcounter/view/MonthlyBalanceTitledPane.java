package org.siki.cashcounter.view;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.Getter;
import org.siki.cashcounter.model.MonthlyBalance;

import java.time.format.DateTimeFormatter;

public class MonthlyBalanceTitledPane extends TitledPane {
  @Getter private final MonthlyBalance monthlyBalance;
  private final ViewFactory viewFactory;

  @Getter private final ObservableList<DailyBalanceControl> dailyBalanceControls;

  private final VBox vbDailyBalances = new VBox();
  private final GridPane content = new GridPane();

  public MonthlyBalanceTitledPane(MonthlyBalance monthlyBalance, ViewFactory viewFactory) {
    //    super(
    //        monthlyBalance.getYearMonth().format(DateTimeFormatter.ofPattern("yyyy.MMMM")),
    //        new GridPane());
    this.setContent(content);
    this.monthlyBalance = monthlyBalance;
    this.viewFactory = viewFactory;

    dailyBalanceControls = FXCollections.observableArrayList();

    loadUI();
  }

  private void loadUI() {
    GridPane.setColumnIndex(vbDailyBalances, 0);
    content.getChildren().addAll(vbDailyBalances /*, gpStatisticsBg*/);
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

    var infoButton = new Button("i");
    //    infoButton.setShape(new Circle(5));
    infoButton.setStyle(
        "-fx-background-color: #bbbbff; "
            + "-fx-background-radius: 5em; "
            + "-fx-min-width: 15px; "
            + "-fx-min-height: 15px; "
            + "-fx-max-width: 15px; "
            + "-fx-max-height: 15px; "
            + "-fx-background-insets: 0px; "
            + "-fx-padding: 0px;"
            + "-fx-margin: 5,5,5,5");
    infoButton.setOnAction(event -> viewFactory.getMonthInfoDialog(monthlyBalance).showAndWait());
    var title =
        new Label(monthlyBalance.getYearMonth().format(DateTimeFormatter.ofPattern("yyyy.MMMM")));
    var header = new HBox(infoButton, title);
    header.setSpacing(5);
    this.setGraphic(header);
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
