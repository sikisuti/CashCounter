package org.siki.cashcounter.view.dialog.monthlyInfo;

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
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.model.DailyBalance;
import org.siki.cashcounter.model.MonthlyBalance;
import org.siki.cashcounter.repository.DataManager;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
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
    TableView<CorrectionDetailsRow> correctionDetailsTable = new TableView<>();
    GridPane.setColumnIndex(correctionDetailsTable, 1);
    TableColumn<CorrectionDetailsRow, String> commentCol = new TableColumn<>("Megnevezés");
    commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));
    TableColumn<CorrectionDetailsRow, String> amountCol = new TableColumn<>("Költés");
    amountCol.setStyle(FX_ALIGNMENT_CENTER_RIGHT);
    amountCol.setCellValueFactory(
        cellData ->
            new SimpleStringProperty(currencyFormat.format(cellData.getValue().getAmount())));
    correctionDetailsTable.getColumns().addAll(commentCol, amountCol);

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
        tableView -> {
          TableRow<CompareRow> tr =
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
              };

          tr.setOnMouseClicked(
              event -> {
                CompareRow cr =
                    ((TableRow<CompareRow>) event.getSource())
                        .getTableView()
                        .getSelectionModel()
                        .getSelectedItem();
                correctionDetailsTable.setItems(getCorrectionDetailsData(cr.getType()));
              });
          return tr;
        });
    summaryTable.getColumns().addAll(categoryCol, predictedCol, spentCol, diffCol);
    summaryTable.setItems(getSummaryTableData());

    var rootGrid = new GridPane();
    rootGrid.setHgap(10);
    rootGrid.setVgap(10);
    rootGrid.getChildren().addAll(summaryTable, correctionDetailsTable);
    this.setScene(new Scene(rootGrid));

    this.initStyle(StageStyle.UTILITY);
    this.setTitle(monthlyBalance.getYearMonth().format(DateTimeFormatter.ofPattern("yyyy.MMMM")));
  }

  private ObservableList<CorrectionDetailsRow> getCorrectionDetailsData(String type) {
    ObservableList<CorrectionDetailsRow> data = FXCollections.observableArrayList();

    monthlyBalance.getDailyBalances().stream()
        .flatMap(db -> db.getCorrections().stream())
        .filter(c -> c.getType().equals(type))
        .forEach(
            c -> {
              data.stream()
                  .filter(cdr -> cdr.getComment().equals(c.getComment()))
                  .findFirst()
                  .ifPresentOrElse(
                      cdr -> cdr.addAmount(c.getAmount()),
                      () ->
                          data.add(
                              CorrectionDetailsRow.builder()
                                  .comment(c.getComment())
                                  .amount(c.getAmount())
                                  .build()));
            });

    return data;
  }

  private ObservableList<CompareRow> getSummaryTableData() {
    ObservableList<CompareRow> data = FXCollections.observableArrayList();

    for (var predictedEntry : monthlyBalance.getPredictions().entrySet()) {
      data.add(
          CompareRow.builder()
              .type(predictedEntry.getKey())
              .predictedAmount(predictedEntry.getValue())
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
}
