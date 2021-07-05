package org.siki.cashcounter.view.dialog;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.model.MonthlyBalance;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class MonthInfoDialog extends Stage {
  private final MonthlyBalance monthlyBalance;

  public MonthInfoDialog(MonthlyBalance monthlyBalance) {
    this.monthlyBalance = monthlyBalance;
    loadUI();
  }

  private void loadUI() {
    var curencyFormat = new DecimalFormat("#,###,###' Ft'");
    TableView<CompareRow> root = new TableView<>();
    TableColumn<CompareRow, String> categoryCol = new TableColumn<>("Kategória");
    categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
    TableColumn<CompareRow, Integer> predictedCol = new TableColumn<>("Tervezett");
    predictedCol.setCellValueFactory(new PropertyValueFactory<>("predictedAmount"));
    TableColumn<CompareRow, Integer> spentCol = new TableColumn<>("Valós");
    spentCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
    TableColumn<CompareRow, String> diffCol = new TableColumn<>("Különbség");
    diffCol.setStyle("-fx-alignment: CENTER-RIGHT;");
    diffCol.setCellValueFactory(
        cellData ->
            new SimpleStringProperty(curencyFormat.format(cellData.getValue().getDifference())));
    root.getColumns().addAll(categoryCol, predictedCol, spentCol, diffCol);
    root.setItems(getTableData());

    this.setScene(new Scene(root));

    this.initModality(Modality.APPLICATION_MODAL);
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
            .collect(Collectors.groupingBy(c -> c.getType()));
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

    return data;
  }
}
