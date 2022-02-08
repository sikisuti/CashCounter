package org.siki.cashcounter.view;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
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
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.MonthlyBalance;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class MonthlyBalanceTitledPane extends TitledPane {
  @Getter private final MonthlyBalance monthlyBalance;
  private final ViewFactory viewFactory;

  @Getter private final ObservableList<DailyBalanceControl> dailyBalanceControls;

  private final VBox vbDailyBalances = new VBox();
  private final GridPane content = new GridPane();

  public MonthlyBalanceTitledPane(MonthlyBalance monthlyBalance, ViewFactory viewFactory) {
    this.setContent(content);
    this.monthlyBalance = monthlyBalance;
    this.viewFactory = viewFactory;

    dailyBalanceControls = FXCollections.observableArrayList();

    loadUI();
  }

  public int addTransactions(List<AccountTransaction> transactions) {
    var dailyGroupedTransactions =
        transactions.stream().collect(Collectors.groupingBy(AccountTransaction::getDate));

    int counter = 0;
    for (var entry : dailyGroupedTransactions.entrySet()) {
      counter +=
          monthlyBalance.getDailyBalances().stream()
              .filter(db -> db.getDate().isEqual(entry.getKey()))
              .findFirst()
              .orElseThrow()
              .addTransactions(entry.getValue());
    }

    return counter;
  }

  private void loadUI() {
    var currencyFormat = NumberFormat.getCurrencyInstance();
    currencyFormat.setMaximumFractionDigits(0);

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
    infoButton.setOnAction(
        event -> {
          var infoDialog = viewFactory.getMonthInfoDialog(monthlyBalance);
          infoDialog.initOwner(this.getScene().getWindow());
          infoDialog.showAndWait();
        });
    var title =
        new Label(monthlyBalance.getYearMonth().format(DateTimeFormatter.ofPattern("yyyy.MMMM")));
    title.setPrefWidth(100);
    var startBalance = new Label();
    startBalance.setPrefWidth(100);
    startBalance.setAlignment(Pos.CENTER_RIGHT);
    startBalance
        .textProperty()
        .bindBidirectional(
            monthlyBalance.getDailyBalances().stream().findFirst().orElseThrow().balanceProperty(),
            currencyFormat);
    var header = new HBox(infoButton, title, startBalance);
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
