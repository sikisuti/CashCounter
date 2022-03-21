package org.siki.cashcounter.view;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lombok.Getter;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.model.DailyBalance;
import org.siki.cashcounter.model.MonthlyBalance;
import org.siki.cashcounter.repository.DataManager;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class MonthlyBalanceTitledPane extends TitledPane {
  @Getter private final MonthlyBalance monthlyBalance;
  private final ViewFactory viewFactory;
  private final DataManager dataManager;

  @Getter private final ObservableList<DailyBalanceControl> dailyBalanceControls;

  private final VBox vbDailyBalances = new VBox();
  private final GridPane content = new GridPane();
  private final Label predictionDifference = new Label();

  private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

  public MonthlyBalanceTitledPane(
      MonthlyBalance monthlyBalance, DataManager dataManager, ViewFactory viewFactory) {
    this.setContent(content);
    this.monthlyBalance = monthlyBalance;
    this.dataManager = dataManager;
    this.viewFactory = viewFactory;

    currencyFormat.setMaximumFractionDigits(0);
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
    predictionDifference
        .onMouseClickedProperty()
        .addListener((observableValue, eventHandler, t1) -> updatePredictionDifference());
    updatePredictionDifference();
    var filler = new Region();
    HBox.setHgrow(filler, Priority.ALWAYS);
    var header = new HBox(infoButton, title, startBalance, filler, predictionDifference);
    header.setSpacing(5);
    header.setPrefWidth(500);
    this.setGraphic(header);
  }

  private void updatePredictionDifference() {
    var predictions =
        monthlyBalance.getPredictions().stream().mapToInt(Correction::getAmount).sum();
    var corrections =
        monthlyBalance.getDailyBalances().stream()
            .flatMap(db -> db.getCorrections().stream())
            .filter(c -> !c.getOnlyMove())
            .mapToInt(Correction::getAmount)
            .sum();
    var predictedUncovered =
        monthlyBalance.getDailyBalances().stream()
            .mapToInt(db -> dataManager.getDayAverage(db.getDate()))
            .sum();
    var actualUncovered =
        monthlyBalance.getDailyBalances().stream()
                .filter(DailyBalance::getPredicted)
                .mapToInt(db -> dataManager.getDayAverage(db.getDate()))
                .sum()
            + monthlyBalance.getDailyBalances().stream()
                .filter(db -> !db.getPredicted())
                .mapToInt(DailyBalance::getUnpairedDailySpent)
                .sum();
    predictions += predictedUncovered;
    corrections += actualUncovered;

    var diff = corrections - predictions;
    predictionDifference.setText(currencyFormat.format(diff));
    if (diff > 50000) {
      predictionDifference.setTextFill(Color.GREEN);
    } else if (diff < -50000) {
      predictionDifference.setTextFill(Color.RED);
    } else {
      predictionDifference.setTextFill(Color.BLACK);
    }

    if (Math.abs(diff) > 100000) {
      predictionDifference.setFont(
          Font.font("Verdana", FontWeight.BOLD, predictionDifference.getFont().getSize()));
    } else {
      predictionDifference.setFont(Font.font("Verdana"));
    }
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
