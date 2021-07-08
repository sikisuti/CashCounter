package org.siki.cashcounter.view.dialog;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
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
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.model.DailyBalance;
import org.siki.cashcounter.service.CorrectionService;

import java.time.format.DateTimeFormatter;

public class CorrectionDialog extends Stage {
  @Getter private final Correction correction;
  private final DailyBalance parentDailyBalance;
  private final BooleanBinding paired;

  ComboBox<String> cbType;
  TextField tfAmount;
  TextField tfComment;
  TableView<AccountTransaction> tblTransactions;

  private final LongProperty pairedTransactionId;

  public CorrectionDialog(
      CorrectionService correctionService, Correction correction, DailyBalance parentDailyBalance) {
    this.correction = correction;
    this.parentDailyBalance = parentDailyBalance;

    pairedTransactionId = new SimpleLongProperty(0);
    paired =
        new BooleanBinding() {
          {
            super.bind(pairedTransactionId);
          }

          @Override
          protected boolean computeValue() {
            return pairedTransactionId.get() != 0;
          }
        };

    loadUI(correctionService);

    pairedTransactionId.set(correction.getPairedTransactionId());
  }

  public CorrectionDialog(CorrectionService correctionService, DailyBalance parentDailyBalance) {
    this(correctionService, new Correction(), parentDailyBalance);
    correction.setId(correctionService.getNextCorrectionId());
    correction.setParentDailyBalance(parentDailyBalance);
  }

  private void loadUI(CorrectionService correctionService) {
    var lblType = new Label("Típus");
    cbType = new ComboBox<>();
    cbType.setEditable(true);
    cbType.setItems(correctionService.getAllCorrectionTypes());
    var lblAmount = new Label("Összeg");
    tfAmount = new TextField();
    tfAmount.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));
    var lblComment = new Label("Megjegyzés");
    tfComment = new TextField();
    var btnRemovePair = new Button("Párosítás törlése");
    btnRemovePair.setOnAction(this::doRemovePair);
    btnRemovePair.visibleProperty().bind(paired);
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
    if (correction.getId() != 0) {
      var btnRemove = new Button("Töröl");
      btnRemove.setOnAction(this::doRemove);
      buttonBar.getButtons().add(btnRemove);
    }
    var btnCancel = new Button("Mégse");
    btnCancel.setOnAction(this::doCancel);
    buttonBar.getButtons().add(btnCancel);
    var root = new VBox(grid, tblTransactions, buttonBar);
    this.setScene(new Scene(root));

    this.initModality(Modality.APPLICATION_MODAL);
    this.initStyle(StageStyle.UTILITY);
    this.setTitle(parentDailyBalance.getDate().format(DateTimeFormatter.ISO_DATE));

    prepareTable();
    parentDailyBalance
        .getTransactions()
        .forEach(t -> tblTransactions.getItems().add(new AccountTransaction(t)));

    cbType.setValue(correction.getType());
    tfAmount.setText(String.valueOf(correction.getAmount()));
    tfComment.setText(correction.getComment());
  }

  private void prepareTable() {
    tblTransactions.setRowFactory(
        tv -> {
          TableRow<AccountTransaction> row = new TableRow<>();
          row.setOnMouseClicked(
              event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                  AccountTransaction rowData = row.getItem();
                  tfAmount.setText(String.valueOf(rowData.getAmount()));
                  if (pairedTransactionId.get() != 0
                      && pairedTransactionId.get() != rowData.getId()) {
                    pairedTransactionId.set(0);
                    rowData.removePairedCorrection(correction);
                  }
                  if (pairedTransactionId.get() != rowData.getId()) {
                    pairedTransactionId.set(rowData.getId());
                    rowData.addPairedCorrection(correction);
                  } else {
                    pairedTransactionId.set(0);
                    rowData.removePairedCorrection(correction);
                  }
                }
              });
          return row;
        });

    TableColumn<AccountTransaction, String> transactionTypeCol =
        new TableColumn<>("Forgalom típusa");
    transactionTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
    TableColumn<AccountTransaction, Integer> amountCol = new TableColumn<>("Összeg");
    amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
    TableColumn<AccountTransaction, String> ownerCol = new TableColumn<>("Ellenoldali név");
    ownerCol.setCellValueFactory(new PropertyValueFactory<>("owner"));
    TableColumn<AccountTransaction, String> commentCol = new TableColumn<>("Közlemény");
    commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));
    TableColumn<AccountTransaction, Boolean> isPairedCol = new TableColumn<>("Párosítva");
    isPairedCol.setCellValueFactory(new PropertyValueFactory<>("paired"));
    isPairedCol.setCellFactory(
        new Callback<>() {
          @Override
          public TableCell<AccountTransaction, Boolean> call(
              TableColumn<AccountTransaction, Boolean> param) {
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
    correction.setAmount(newAmount);
    correction.setType(cbType.getValue());
    correction.setComment(tfComment.getText());

    if (correction.getPairedTransactionId() != 0) {
      parentDailyBalance
          .getTransactionById(correction.getPairedTransactionId())
          .ifPresent(t -> t.removePairedCorrection(correction));
    }

    parentDailyBalance
        .getTransactionById(pairedTransactionId.get())
        .ifPresent(t -> t.addPairedCorrection(correction));
    correction.setPairedTransactionId(pairedTransactionId.get());
    parentDailyBalance.addCorrection(correction);

    this.close();
  }

  protected void doCancel(ActionEvent event) {
    this.close();
  }

  protected void doRemove(ActionEvent event) {
    parentDailyBalance.removeCorrection(correction);
    this.close();
  }

  protected void doRemovePair(ActionEvent event) {
    tblTransactions.getItems().stream()
        .filter(t -> t.getId() == pairedTransactionId.get())
        .findFirst()
        .ifPresent(t -> t.removePairedCorrection(correction));
    pairedTransactionId.set(0);
  }
}
