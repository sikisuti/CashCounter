package org.siki.cashcounter.view.dialog.monthlyinfo;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Labeled;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.model.DailyBalance;
import org.siki.cashcounter.model.MonthlyBalance;
import org.siki.cashcounter.repository.DataManager;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class MonthInfoDialog extends Stage {
  private static final DecimalFormat currencyFormat = new DecimalFormat("#,###,###' Ft'");
  private static final String FX_ALIGNMENT_CENTER_RIGHT = "-fx-alignment: CENTER-RIGHT;";
  private final MonthlyBalance monthlyBalance;
  private final DataManager dataManager;

  public MonthInfoDialog(MonthlyBalance monthlyBalance, DataManager dataManager) {
    this.monthlyBalance = monthlyBalance;
    this.dataManager = dataManager;
    loadUI();
  }

  private void loadUI() {
    this.setScene(new Scene(initRootGrid()));

    this.initStyle(StageStyle.UTILITY);
    this.setTitle(monthlyBalance.getYearMonth().format(DateTimeFormatter.ofPattern("yyyy.MMMM")));
  }

  private GridPane initRootGrid() {
    var rootGrid = new GridPane();
    rootGrid.setHgap(10);
    rootGrid.setVgap(10);
    var summaryTable = initSummaryTable();
    var detailsTable = initDetailsTable(summaryTable);
    var unpairedTransactionTable = initUnpairedTransactionsTable();
    rootGrid.getChildren().addAll(summaryTable, detailsTable, unpairedTransactionTable);
    return rootGrid;
  }

  private TableView<CompareRow> initSummaryTable() {
    TableView<CompareRow> summaryTable = new TableView<>();
    summaryTable.setPrefWidth(400);
    summaryTable.setPrefHeight(450);
    TableColumn<CompareRow, String> categoryCol = new TableColumn<>("Típus");
    categoryCol.setCellValueFactory(new PropertyValueFactory<>("type"));
    TableColumn<CompareRow, String> predictedCol = new TableColumn<>("Tervezett");
    predictedCol.setStyle(FX_ALIGNMENT_CENTER_RIGHT);
    predictedCol.setCellValueFactory(
        cellData ->
            new SimpleStringProperty(
                currencyFormat.format(cellData.getValue().getPredictedAmount())));
    TableColumn<CompareRow, String> spentCol = new TableColumn<>("Valós");
    spentCol.setStyle(FX_ALIGNMENT_CENTER_RIGHT);
    spentCol.setCellValueFactory(
        cellData ->
            new SimpleStringProperty(currencyFormat.format(cellData.getValue().getAmount())));
    TableColumn<CompareRow, String> diffCol = new TableColumn<>("Különbség");
    diffCol.setStyle(FX_ALIGNMENT_CENTER_RIGHT);
    diffCol.setCellValueFactory(
        cellData ->
            new SimpleStringProperty(currencyFormat.format(cellData.getValue().getDifference())));
    summaryTable.setRowFactory(
        tableView ->
            new TableRow<>() {
              @Override
              protected void updateItem(CompareRow item, boolean empty) {
                var style = new StringBuilder();
                if (!empty) {
                  if (item.bold) {
                    style.append("-fx-font-weight: bold;");
                  }

                  var diff = item.amount - item.predictedAmount;
                  if (diff > -10000 && diff < 10000) {
                    getChildren().forEach(cell -> ((Labeled) cell).setTextFill(Color.LIGHTGRAY));
                  } else if (diff < -50000) {
                    getChildren().forEach(cell -> ((Labeled) cell).setTextFill(Color.FIREBRICK));
                  } else if (diff > 50000) {
                    getChildren().forEach(cell -> ((Labeled) cell).setTextFill(Color.SEAGREEN));
                  }

                  setStyle(style.toString());
                }
              }
            });
    summaryTable.getColumns().addAll(categoryCol, predictedCol, spentCol, diffCol);
    summaryTable.setItems(getSummaryTableData());
    return summaryTable;
  }

  private TableView<CompareRow> initDetailsTable(TableView<CompareRow> summaryTable) {
    TableView<CompareRow> correctionDetailsTable = new TableView<>();
    correctionDetailsTable.setPrefHeight(300);
    GridPane.setRowIndex(correctionDetailsTable, 1);
    TableColumn<CompareRow, String> commentCol = new TableColumn<>("Megjegyzés");
    commentCol.setCellValueFactory(new PropertyValueFactory<>("type"));
    TableColumn<CompareRow, String> predictedCol = new TableColumn<>("Tervezett");
    predictedCol.setStyle(FX_ALIGNMENT_CENTER_RIGHT);
    predictedCol.setCellValueFactory(
        cellData ->
            new SimpleStringProperty(
                currencyFormat.format(cellData.getValue().getPredictedAmount())));
    TableColumn<CompareRow, String> amountCol = new TableColumn<>("Valós");
    amountCol.setStyle(FX_ALIGNMENT_CENTER_RIGHT);
    amountCol.setCellValueFactory(
        cellData ->
            new SimpleStringProperty(currencyFormat.format(cellData.getValue().getAmount())));
    TableColumn<CompareRow, String> diffCol = new TableColumn<>("Különbség");
    diffCol.setStyle(FX_ALIGNMENT_CENTER_RIGHT);
    diffCol.setCellValueFactory(
        cellData ->
            new SimpleStringProperty(currencyFormat.format(cellData.getValue().getDifference())));
    correctionDetailsTable.setRowFactory(
        tableView ->
            new TableRow<>() {
              @Override
              protected void updateItem(CompareRow item, boolean empty) {
                if (!empty) {
                  var diff = item.amount - item.predictedAmount;
                  if (diff > -3000 && diff < 3000) {
                    getChildren().forEach(cell -> ((Labeled) cell).setTextFill(Color.LIGHTGRAY));
                  } else if (diff < -10000) {
                    getChildren().forEach(cell -> ((Labeled) cell).setTextFill(Color.FIREBRICK));
                  } else if (diff > 10000) {
                    getChildren().forEach(cell -> ((Labeled) cell).setTextFill(Color.SEAGREEN));
                  }
                }
              }
            });
    correctionDetailsTable.getColumns().addAll(commentCol, predictedCol, amountCol, diffCol);

    summaryTable
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (observable, oldValue, newValue) ->
                correctionDetailsTable.setItems(getCorrectionDetailsData(newValue.getType())));

    return correctionDetailsTable;
  }

  private ObservableList<CompareRow> getCorrectionDetailsData(String type) {
    ObservableList<CompareRow> data = FXCollections.observableArrayList();

    monthlyBalance.getPredictions().stream()
        .filter(c -> c.getType().equals(type))
        .forEach(
            c ->
                data.stream()
                    .filter(cr -> cr.getType().equals(c.getComment()))
                    .findFirst()
                    .ifPresentOrElse(
                        cr -> cr.setAmount(cr.getPredictedAmount() + c.getAmount()),
                        () ->
                            data.add(
                                CompareRow.builder()
                                    .type(c.getComment())
                                    .predictedAmount(c.getAmount())
                                    .build())));

    monthlyBalance.getDailyBalances().stream()
        .flatMap(db -> db.getCorrections().stream())
        .filter(c -> c.getType().equals(type))
        .forEach(
            c ->
                data.stream()
                    .filter(cr -> cr.getType().equals(c.getComment()))
                    .findFirst()
                    .ifPresentOrElse(
                        cr -> cr.setAmount(cr.getAmount() + c.getAmount()),
                        () ->
                            data.add(
                                CompareRow.builder()
                                    .type(c.getComment())
                                    .amount(c.getAmount())
                                    .build())));
    return data;
  }

  private ObservableList<CompareRow> getSummaryTableData() {
    ObservableList<CompareRow> data = FXCollections.observableArrayList();

    var groupedPredictedCorrections =
        monthlyBalance.getPredictions().stream()
            .collect(Collectors.groupingBy(Correction::getType, TreeMap::new, Collectors.toList()));

    for (var predictedCorrectionGroupEntry : groupedPredictedCorrections.entrySet()) {
      data.add(
          CompareRow.builder()
              .type(predictedCorrectionGroupEntry.getKey())
              .predictedAmount(
                  predictedCorrectionGroupEntry.getValue().stream()
                      .mapToInt(Correction::getAmount)
                      .sum())
              .build());
    }

    var groupedCorrections =
        monthlyBalance.getDailyBalances().stream()
            .flatMap(db -> db.getCorrections().stream())
            .collect(Collectors.groupingBy(Correction::getType));
    for (var correctionGroup : groupedCorrections.entrySet()) {
      var dataItem =
          data.stream()
              .filter(cr -> cr.getType().equalsIgnoreCase(correctionGroup.getKey()))
              .findFirst()
              .orElseGet(
                  () -> {
                    var newData = CompareRow.builder().type(correctionGroup.getKey()).build();
                    data.add(newData);
                    return newData;
                  });

      dataItem.setAmount(correctionGroup.getValue().stream().mapToInt(Correction::getAmount).sum());
    }

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
    var unCoveredRow =
        CompareRow.builder()
            .type("Általános")
            .predictedAmount(predictedUncovered)
            .amount(actualUncovered)
            .build();
    data.add(unCoveredRow);

    var summaryRow =
        CompareRow.builder()
            .type("Összesen")
            .predictedAmount(data.stream().mapToInt(d -> d.predictedAmount).sum())
            .amount(data.stream().mapToInt(d -> d.amount).sum())
            .bold(true)
            .build();
    data.add(summaryRow);

    return data;
  }

  private TableView<CompareRow> initUnpairedTransactionsTable() {
    TableView<CompareRow> unpairedTransactionsTable = new TableView<>();
    unpairedTransactionsTable.setPrefWidth(500);
    GridPane.setColumnIndex(unpairedTransactionsTable, 1);
    GridPane.setRowSpan(unpairedTransactionsTable, 2);
    TableColumn<CompareRow, String> commentCol = new TableColumn<>("Hely");
    commentCol.setCellValueFactory(new PropertyValueFactory<>("type"));
    TableColumn<CompareRow, String> amountCol = new TableColumn<>("Költés");
    amountCol.setStyle(FX_ALIGNMENT_CENTER_RIGHT);
    amountCol.setCellValueFactory(
        cellData ->
            new SimpleStringProperty(currencyFormat.format(cellData.getValue().getAmount())));
    unpairedTransactionsTable.setRowFactory(
        tableView ->
            new TableRow<>() {
              @Override
              protected void updateItem(CompareRow item, boolean empty) {
                var style = new StringBuilder();
                if (!empty) {
                  if (item.bold) {
                    style.append("-fx-font-weight: bold;");
                  }

                  setStyle(style.toString());
                }
              }
            });
    unpairedTransactionsTable.getColumns().addAll(commentCol, amountCol);
    unpairedTransactionsTable.setItems(getUnpairedTransactions());

    return unpairedTransactionsTable;
  }

  private ObservableList<CompareRow> getUnpairedTransactions() {
    ObservableList<CompareRow> data = FXCollections.observableArrayList();

    var sumAmount = 0;

    var byCategoryTransactions =
        monthlyBalance.getDailyBalances().stream()
            .flatMap(db -> db.getTransactions().stream())
            .filter(t -> t.getUnpairedAmount() != 0)
            .collect(Collectors.groupingBy(AccountTransaction::getCategory));

    for (var byCategoryEntry : byCategoryTransactions.entrySet()) {
      var byOwnerTransactions =
          byCategoryEntry.getValue().stream()
              .collect(Collectors.groupingBy(AccountTransaction::getOwner));
      for (var byOwnerEntry : byOwnerTransactions.entrySet()) {
        var byTypeTransactions =
            byOwnerEntry.getValue().stream()
                .collect(Collectors.groupingBy(AccountTransaction::getType));
        for (var byTypeEntry : byTypeTransactions.entrySet()) {
          data.add(
              CompareRow.builder()
                  .type(byOwnerEntry.getKey() + "\t- " + byTypeEntry.getKey())
                  .amount(
                      byTypeEntry.getValue().stream()
                          .mapToInt(AccountTransaction::getUnpairedAmount)
                          .sum())
                  .build());
        }
      }

      var groupAmount =
          byCategoryEntry.getValue().stream().mapToInt(AccountTransaction::getUnpairedAmount).sum();
      data.add(
          CompareRow.builder()
              .type(byCategoryEntry.getKey() + " összesen")
              .amount(groupAmount)
              .bold(true)
              .build());
      sumAmount += groupAmount;
    }

    data.add(CompareRow.builder().type("Összesen").amount(sumAmount).bold(true).build());

    return data;
  }
}
