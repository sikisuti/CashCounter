package org.siki.cashcounter.view.dialog;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.converter.NumberStringConverter;
import lombok.Getter;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.service.DataForViewService;
import org.siki.cashcounter.view.DailyBalanceControl;
import org.siki.cashcounter.view.model.ObservableTransaction;

import java.time.format.DateTimeFormatter;

public class CorrectionDialog extends Stage {
  @Getter private final Correction correction;
  private final DailyBalanceControl parentDailyBalanceControl;
  private final BooleanProperty pairedProperty = new SimpleBooleanProperty(false);

  ComboBox<String> cbType;
  TextField tfAmount;
  TextField tfComment;
  TableView<ObservableTransaction> tblTransactions;

  public CorrectionDialog(
      DataForViewService dataForViewService,
      Correction correction,
      DailyBalanceControl parentDailyBalanceControl) {
    this.correction = correction;
    this.parentDailyBalanceControl = parentDailyBalanceControl;

    loadUI(dataForViewService);
  }

  public CorrectionDialog(
      DataForViewService dataForViewService, DailyBalanceControl parentDailyBalanceControl) {
    this(dataForViewService, new Correction(), parentDailyBalanceControl);
  }

  private void loadUI(DataForViewService dataForViewService) {
    var lblType = new Label("Típus");
    cbType = new ComboBox<>();
    cbType.setEditable(true);
    cbType.setItems(dataForViewService.getAllCorrectionTypes());
    var lblAmount = new Label("Összeg");
    tfAmount = new TextField();
    tfAmount.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));
    var lblComment = new Label("Megjegyzés");
    tfComment = new TextField();
    var btnRemovePair = new Button("Párosítás törlése");
    btnRemovePair.setOnAction(this::doRemovePair);
    btnRemovePair.visibleProperty().bind(pairedProperty);
    var grid = new GridPane();
    grid.getColumnConstraints()
        .addAll(new ColumnConstraints(), new ColumnConstraints(), new ColumnConstraints());
    grid.getRowConstraints()
        .addAll(new RowConstraints(), new RowConstraints(), new RowConstraints());
    grid.getChildren()
        .addAll(lblType, cbType, lblAmount, tfAmount, lblComment, tfComment, btnRemovePair);
    GridPane.setColumnIndex(cbType, 1);
    GridPane.setRowIndex(lblAmount, 1);
    GridPane.setColumnIndex(tfAmount, 1);
    GridPane.setRowIndex(tfAmount, 1);
    GridPane.setRowIndex(lblComment, 2);
    GridPane.setColumnIndex(tfComment, 1);
    GridPane.setRowIndex(tfComment, 2);
    GridPane.setColumnIndex(btnRemovePair, 3);
    GridPane.setRowSpan(btnRemovePair, 3);
    tblTransactions = new TableView<>();
    tblTransactions.setPrefWidth(1000);
    tblTransactions.setPrefHeight(300);
    var buttonBar = new ButtonBar();
    var btnSave = new Button("Mentés");
    btnSave.setOnAction(this::doSave);
    buttonBar.getButtons().add(btnSave);
    var btnCancel = new Button("Mégse");
    btnCancel.setOnAction(this::doCancel);
    buttonBar.getButtons().add(btnCancel);
    var root = new VBox(grid, tblTransactions, buttonBar);
    this.setScene(new Scene(root));

    this.initModality(Modality.APPLICATION_MODAL);
    this.initStyle(StageStyle.UTILITY);
    this.setTitle(parentDailyBalanceControl.getDate().format(DateTimeFormatter.ISO_DATE));

    prepareTable();
    tblTransactions.setItems(parentDailyBalanceControl.getTransactions());

    cbType.setValue(correction.getType());
    tfAmount.setText(String.valueOf(correction.getAmount()));
    tfComment.setText(correction.getComment());

    pairedProperty.set(correction.isPaired());
  }

  private void prepareTable() {
    tblTransactions.setRowFactory(
        tv -> {
          TableRow<ObservableTransaction> row = new TableRow<>();
          row.setOnMouseClicked(
              event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                  ObservableTransaction rowData = row.getItem();
                  tfAmount.setText(String.valueOf(rowData.getAmount()));
                  if (correction.getPairedTransactionId() != 0
                      && correction.getPairedTransactionId() != rowData.getId()) {
                    correction.setPairedTransaction(null);
                  }
                  if (correction.getPairedTransactionId() != rowData.getId()) {
                    rowData.addPairedCorrection(observableCorrection);
                    observableCorrection.setPairedTransaction(rowData);
                  } else {
                    observableCorrection.setPairedTransaction(null);
                  }
                }
              });
          return row;
        });

    TableColumn<ObservableTransaction, String> transactionTypeCol =
        new TableColumn<>("Forgalom típusa");
    transactionTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
    TableColumn<ObservableTransaction, Integer> amountCol = new TableColumn<>("Összeg");
    amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
    TableColumn<ObservableTransaction, String> ownerCol = new TableColumn<>("Ellenoldali név");
    ownerCol.setCellValueFactory(new PropertyValueFactory<>("owner"));
    TableColumn<ObservableTransaction, String> commentCol = new TableColumn<>("Közlemény");
    commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));
    TableColumn<ObservableTransaction, Boolean> isPairedCol = new TableColumn<>("Párosítva");
    isPairedCol.setCellValueFactory(new PropertyValueFactory<>("paired"));
    isPairedCol.setCellFactory(
        new Callback<>() {
          @Override
          public TableCell<ObservableTransaction, Boolean> call(
              TableColumn<ObservableTransaction, Boolean> param) {
            return new TableCell<>() {

              @Override
              protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || !item) {
                  setText(null);
                  setGraphic(null);
                } else {
                  setGraphic(new Circle(10, new Color(0, 0, 1, 1)));
                }
              }
            };
          }
        });
    tblTransactions
        .getColumns()
        .setAll(transactionTypeCol, amountCol, isPairedCol, ownerCol, commentCol);
  }

  protected void doSave(ActionEvent event) {
    var newAmount = Integer.parseInt(tfAmount.getText().replaceAll("[^0-9\\-]", ""));
    observableCorrection.setAmount(newAmount);
    observableCorrection.setType(cbType.getValue());
    observableCorrection.setComment(tfComment.getText());
    parentDailyBalanceControl.addCorrection(observableCorrection);

    this.close();
  }

  protected void doCancel(ActionEvent event) {
    this.close();
  }

  // TODO:
  protected void doRemove(ActionEvent event) {
    if (observableCorrection.getPairedTransaction() != null) {
      observableCorrection.getPairedTransaction().removePairedCorrection(observableCorrection);
    }

    parentDailyBalanceControl.removeCorrection(observableCorrection);
    this.close();
  }

  protected void doRemovePair(ActionEvent event) {
    if (observableCorrection.getPairedTransaction() != null) {
      observableCorrection.getPairedTransaction().removePairedCorrection(observableCorrection);
    }
    observableCorrection.setPairedTransaction(null);
    //    observableCorrection.pairedTransactionIdProperty().set(0);
    //        pairedTransaction = null;
  }
}
