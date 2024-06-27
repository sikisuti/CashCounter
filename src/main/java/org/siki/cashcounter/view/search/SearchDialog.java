package org.siki.cashcounter.view.search;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static javafx.stage.StageStyle.DECORATED;

import java.text.NumberFormat;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.repository.DataManager;

public class SearchDialog extends Stage {
  private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
  private static final String FX_ALIGNMENT_CENTER_RIGHT = "-fx-alignment: CENTER-RIGHT;";

  private final TextField searchText = new TextField();
  private final TableView<Correction> searchResultTable = new TableView<>();

  private final List<Correction> corrections;

  public SearchDialog(DataManager dataManager) {
    loadUI();

    searchText.setOnAction(this::doSearch);

    corrections = dataManager.getAllCorrections();
    corrections.sort(
        (o1, o2) ->
            o2.getParentDailyBalance().getDate().compareTo(o1.getParentDailyBalance().getDate()));
  }

  private void loadUI() {
    this.setScene(new Scene(initContent()));

    this.initStyle(DECORATED);
    this.setTitle("Keresés");
  }

  private Pane initContent() {
    var content = new VBox(searchRow(), searchResultTable());
    content.setSpacing(10.0);
    return content;
  }

  private HBox searchRow() {
    HBox.setMargin(searchText, new Insets(20.0));
    return new HBox(searchText);
  }

  private TableView<Correction> searchResultTable() {
    searchResultTable.setPrefHeight(500);
    searchResultTable.setPrefWidth(500);
    TableColumn<Correction, String> dateCol = new TableColumn<>("Dátum");
    dateCol.setCellValueFactory(
        cellData ->
            new SimpleStringProperty(
                cellData.getValue().getParentDailyBalance().getDate().format(ISO_DATE)));
    TableColumn<Correction, String> categoryCol = new TableColumn<>("Típus");
    categoryCol.setCellValueFactory(new PropertyValueFactory<>("type"));
    TableColumn<Correction, String> commentCol = new TableColumn<>("Megjegyzés");
    commentCol.setPrefWidth(150.0);
    commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));
    TableColumn<Correction, String> amountCol = new TableColumn<>("Összeg");
    amountCol.setPrefWidth(100.0);
    amountCol.setStyle(FX_ALIGNMENT_CENTER_RIGHT);
    amountCol.setCellValueFactory(
        cellData ->
            new SimpleStringProperty(currencyFormat.format(cellData.getValue().getAmount())));
    searchResultTable.getColumns().addAll(dateCol, categoryCol, commentCol, amountCol);

    return searchResultTable;
  }

  private void doSearch(ActionEvent actionEvent) {
    var resultList =
        corrections.stream()
            .filter(
                c ->
                    c.getType().toLowerCase().contains(searchText.getText().toLowerCase())
                        || c.getComment()
                            .toLowerCase()
                            .contains(searchText.getText().toLowerCase()))
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
    searchResultTable.getItems().clear();
    searchResultTable.setItems(resultList);
  }
}
