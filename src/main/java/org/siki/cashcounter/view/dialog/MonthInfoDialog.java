package org.siki.cashcounter.view.dialog;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
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
    TableView<CompareRow> root = new TableView<>();
    TableColumn<CompareRow, String> categoryCol = new TableColumn<>("Kategória");
    categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
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
    root.setRowFactory(
        tableView ->
            new TableRow<>() {
              @Override
              protected void updateItem(CompareRow item, boolean empty) {
                if (!empty && item.bold) {
                  setStyle("-fx-font-weight: bold");
                }
              }
            });
    root.getColumns().addAll(categoryCol, predictedCol, spentCol, diffCol);
    root.setItems(getTableData());

    this.setScene(new Scene(root));

    this.initStyle(StageStyle.UTILITY);
    this.setTitle(monthlyBalance.getYearMonth().format(DateTimeFormatter.ofPattern("yyyy.MMMM")));
  }

  private ObservableList<CompareRow> getTableData() {
    ObservableList<CompareRow> data = FXCollections.observableArrayList();

    for (var predictedEntry : monthlyBalance.getPredictions().entrySet()) {
      data.add(
          CompareRow.builder()
              .category(predictedEntry.getKey())
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
              .filter(cr -> cr.getCategory().equalsIgnoreCase(correctionGroup.getKey()))
              .findFirst()
              .orElseGet(
                  () -> {
                    var newData = CompareRow.builder().category(correctionGroup.getKey()).build();
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
            .category("Általános")
            .predictedAmount(predictedUncovered)
            .amount(actualUncovered)
            .build();
    data.add(unCoveredRow);

    var summaryRow =
        CompareRow.builder()
            .category("Összesen")
            .predictedAmount(data.stream().mapToInt(d -> d.predictedAmount).sum())
            .amount(data.stream().mapToInt(d -> d.amount).sum())
            .bold(true)
            .build();
    data.add(summaryRow);

    return data;
  }
}
