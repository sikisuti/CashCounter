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
import org.siki.cashcounter.view.model.ObservableAccountTransaction;
import org.siki.cashcounter.view.model.ObservableCorrection;

public class CorrectionDialog extends Stage {
  @Getter private final ObservableCorrection observableCorrection;
  private final DailyBalanceControl parentDailyBalanceControl;
  private final BooleanProperty pairedProperty = new SimpleBooleanProperty(false);

  ComboBox<String> cbType;
  TextField tfAmount;
  TextField tfComment;
  TableView<ObservableAccountTransaction> tblTransactions;

  public CorrectionDialog(
      DataForViewService dataForViewService,
      ObservableCorrection observableCorrection,
      DailyBalanceControl parentDailyBalanceControl) {
    this.observableCorrection = observableCorrection;
    this.parentDailyBalanceControl = parentDailyBalanceControl;

    loadUI(dataForViewService);
  }

  public CorrectionDialog(
      DataForViewService dataForViewService, DailyBalanceControl parentDailyBalanceControl) {
    this(
        dataForViewService,
        ObservableCorrection.of(
            new Correction(), parentDailyBalanceControl.getObservableDailyBalance()),
        parentDailyBalanceControl);
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
    this.setTitle(parentDailyBalanceControl.getDate());

    prepareTable();
    tblTransactions.setItems(parentDailyBalanceControl.getObservableTransactions());

    cbType.setValue(observableCorrection.typeProperty().get());
    tfAmount.setText(String.valueOf(observableCorrection.amountProperty().get()));
    tfComment.setText(observableCorrection.commentProperty().get());

    pairedProperty.bind(observableCorrection.pairedProperty());
  }

  private void prepareTable() {
    tblTransactions.setRowFactory(
        tv -> {
          TableRow<ObservableAccountTransaction> row = new TableRow<>();
          row.setOnMouseClicked(
              event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                  ObservableAccountTransaction rowData = row.getItem();
                  tfAmount.setText(String.valueOf(rowData.getAmount()));
                  if (observableCorrection.getPairedTransaction() != null
                      && observableCorrection.getPairedTransaction() != rowData) {
                    observableCorrection
                        .getPairedTransaction()
                        .removePairedCorrection(observableCorrection);
                  }
                  if (observableCorrection.getPairedTransaction() != rowData) {
                    rowData.addPairedCorrection(observableCorrection);
                    observableCorrection.setPairedTransaction(rowData);
                  } else {
                    observableCorrection.setPairedTransaction(null);
                  }
                }
              });
          return row;
        });

    TableColumn<ObservableAccountTransaction, String> transactionTypeCol =
        new TableColumn<>("Forgalom típusa");
    transactionTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
    TableColumn<ObservableAccountTransaction, Integer> amountCol = new TableColumn<>("Összeg");
    amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
    TableColumn<ObservableAccountTransaction, String> ownerCol =
        new TableColumn<>("Ellenoldali név");
    ownerCol.setCellValueFactory(new PropertyValueFactory<>("owner"));
    TableColumn<ObservableAccountTransaction, String> commentCol = new TableColumn<>("Közlemény");
    commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));
    TableColumn<ObservableAccountTransaction, Boolean> isPairedCol = new TableColumn<>("Párosítva");
    isPairedCol.setCellValueFactory(new PropertyValueFactory<>("paired"));
    isPairedCol.setCellFactory(
        new Callback<>() {
          @Override
          public TableCell<ObservableAccountTransaction, Boolean> call(
              TableColumn<ObservableAccountTransaction, Boolean> param) {
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
