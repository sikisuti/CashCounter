package org.siki.cashcounter.view.dialog;

import static javafx.stage.StageStyle.DECORATED;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.model.MonthlyBalance;

public class MonthlyPredictionsDialog extends Stage {
  private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
  private final MonthlyBalance monthlyBalance;

  TableView<Correction> predictionsTable = new TableView<>();
  TextField category = new TextField();
  TextField comment = new TextField();
  TextField amount = new TextField();

  public MonthlyPredictionsDialog(MonthlyBalance monthlyBalance) {
    this.monthlyBalance = monthlyBalance;
    currencyFormat.setMaximumFractionDigits(0);
    loadUI();
  }

  private void loadUI() {
    predictionsTable();
    var root = new VBox(predictionsTable, actionButtonsView(), addItemView());
    root.setSpacing(20);
    root.setPadding(new Insets(20));
    this.setScene(new Scene(root));

    this.initStyle(DECORATED);
    this.setTitle(monthlyBalance.getYearMonth().format(DateTimeFormatter.ofPattern("yyyy.MMMM")));
  }

  private void predictionsTable() {
    predictionsTable = new TableView<>();
    TableColumn<Correction, String> categoryCol = new TableColumn<>("Kategória");
    categoryCol.setCellValueFactory(new PropertyValueFactory<>("type"));
    TableColumn<Correction, String> commentCol = new TableColumn<>("Megnevezés");
    commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));
    TableColumn<Correction, String> amountCol = new TableColumn<>("Összeg");
    amountCol.setCellValueFactory(
        cellData ->
            new SimpleStringProperty(currencyFormat.format(cellData.getValue().getAmount())));
    predictionsTable.getColumns().addAll(categoryCol, commentCol, amountCol);
    predictionsTable.setItems(FXCollections.observableList(monthlyBalance.getPredictions()));
  }

  private Node actionButtonsView() {
    var hbox = new HBox(removeItemView(), moveToPrevMonthView(), moveToNextMonthView());
    hbox.setSpacing(20.0);
    return hbox;
  }

  private Node removeItemView() {
    var btn = new Button("töröl");
    btn.setOnAction(
        actionEvent -> {
          if (!predictionsTable.getSelectionModel().isEmpty()) {
            predictionsTable
                .getItems()
                .remove(predictionsTable.getSelectionModel().getSelectedIndex());
          }
        });

    return btn;
  }

  private Node moveToPrevMonthView() {
    var btn = new Button("vissza mozgat");
    btn.setOnAction(
        actionEvent -> {
          if (!predictionsTable.getSelectionModel().isEmpty()) {
            var item = predictionsTable.getSelectionModel().getSelectedItem();
            predictionsTable
                .getItems()
                .remove(predictionsTable.getSelectionModel().getSelectedIndex());
            monthlyBalance.getPreviousMonthlyBalance().getPredictions().add(item);
          }
        });

    return btn;
  }

  private Node moveToNextMonthView() {
    var btn = new Button("előre mozgat");
    btn.setOnAction(
        actionEvent -> {
          if (!predictionsTable.getSelectionModel().isEmpty()) {
            var item = predictionsTable.getSelectionModel().getSelectedItem();
            predictionsTable
                .getItems()
                .remove(predictionsTable.getSelectionModel().getSelectedIndex());
            monthlyBalance.getNextMonthlyBalance().getPredictions().add(item);
          }
        });

    return btn;
  }

  private Node addItemView() {
    var addButton = new Button("Hozzáad");
    addButton.setOnAction(this::addItem);
    var container = new HBox(category, comment, amount, addButton);
    container.setSpacing(10);

    return container;
  }

  private void addItem(ActionEvent actionEvent) {
    if (!category.getText().isEmpty()
        && !comment.getText().isEmpty()
        && !amount.getText().isEmpty()) {
      var itemToAdd = new Correction();
      itemToAdd.setType(category.getText());
      itemToAdd.setComment(comment.getText());
      itemToAdd.setAmount(Integer.parseInt(amount.getText()));
      predictionsTable.getItems().add(itemToAdd);
      category.clear();
      comment.clear();
      amount.clear();
    }
  }
}
