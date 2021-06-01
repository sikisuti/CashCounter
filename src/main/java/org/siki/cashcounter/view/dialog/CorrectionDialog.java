package org.siki.cashcounter.view.dialog;

import com.siki.cashcount.model.AccountTransaction;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
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
import javafx.util.Callback;
import javafx.util.converter.NumberStringConverter;
import org.siki.cashcounter.service.DataForViewService;
import org.siki.cashcounter.view.DailyBalanceControl;
import org.siki.cashcounter.view.model.ObservableAccountTransaction;
import org.siki.cashcounter.view.model.ObservableCorrection;

public class CorrectionDialog extends Dialog<ObservableCorrection> {
  private ObservableCorrection observableCorrection;
  private DailyBalanceControl dailyBalanceControl;

  ComboBox<String> cbType;
  TextField tfAmount;
  TextField tfComment;
  TableView<ObservableAccountTransaction> tblTransactions;

  public CorrectionDialog(
      DataForViewService dataForViewService,
      ObservableCorrection observableCorrection,
      DailyBalanceControl dailyBalanceControl) {
    this(dataForViewService);

    cbType.setValue(observableCorrection.typeProperty().get());
    tfAmount.setText(String.valueOf(observableCorrection.amountProperty().get()));
    tfComment.setText(observableCorrection.commentProperty().get());

    this.observableCorrection = observableCorrection;
    this.dailyBalanceControl = dailyBalanceControl;

    prepareTable();
    tblTransactions.setItems(
        dailyBalanceControl.getObservableDailyBalance().getObservableTransactions());
  }

  public CorrectionDialog(DataForViewService dataForViewService) {
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
    var root = new VBox(grid, tblTransactions);
    this.getDialogPane().setContent(root);
  }

  private void prepareTable() {
    tblTransactions.setRowFactory(
        tv -> {
          TableRow<ObservableAccountTransaction> row = new TableRow<>();
          row.setOnMouseClicked(
              event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                  ObservableAccountTransaction rowData = row.getItem();
                  observableCorrection.amountProperty().set(rowData.amountProperty().get());
                  tfAmount.setText(String.valueOf(rowData.amountProperty().get()));
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

    TableColumn<AccountTransaction, String> transactionTypeCol =
        new TableColumn<>("Forgalom típusa");
    transactionTypeCol.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
    //        TableColumn<AccountTransaction, LocalDate> dateCol = new TableColumn<>("Könyvelési
    // dátum");
    //        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
    TableColumn<AccountTransaction, Integer> amountCol = new TableColumn<>("Összeg");
    amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
    //        TableColumn<AccountTransaction, Integer> balanceCol = new TableColumn<>("Új könyvelt
    // egyenleg");
    //        balanceCol.setCellValueFactory(new PropertyValueFactory<>("balance"));
    //        TableColumn<AccountTransaction, String> accountNumberCol = new
    // TableColumn<>("Ellenoldali számlaszám");
    //        accountNumberCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
    TableColumn<AccountTransaction, String> ownerCol = new TableColumn<>("Ellenoldali név");
    ownerCol.setCellValueFactory(new PropertyValueFactory<>("owner"));
    TableColumn<AccountTransaction, String> commentCol = new TableColumn<>("Közlemény");
    commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));
    //        TableColumn<AccountTransaction, String> counterCol = new TableColumn<>("?");
    //        counterCol.setCellValueFactory(new PropertyValueFactory<>("counter"));
    TableColumn<AccountTransaction, Boolean> isPairedCol = new TableColumn<>("Párosítva");
    isPairedCol.setCellValueFactory(new PropertyValueFactory<>("paired"));
    isPairedCol.setCellFactory(
        new Callback<
            TableColumn<AccountTransaction, Boolean>, TableCell<AccountTransaction, Boolean>>() {
          @Override
          public TableCell<AccountTransaction, Boolean> call(
              TableColumn<AccountTransaction, Boolean> param) {
            return new TableCell<AccountTransaction, Boolean>() {
              {
              }

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

  @FXML
  protected void doSave(ActionEvent event) {
    observableCorrection.setAmount(
        Integer.parseInt(tfAmount.getText().replaceAll("[^0-9\\-]", "")));
    observableCorrection.setType(cbType.getValue());
    observableCorrection.setComment(tfComment.getText());

    okClicked = true;

    dialogStage.close();
  }

  @FXML
  protected void doCancel(ActionEvent event) {
    dialogStage.close();
  }

  @FXML
  protected void doRemove(ActionEvent event) {
    if (observableCorrection.getPairedTransaction() != null) {
      observableCorrection.getPairedTransaction().removePairedCorrection(observableCorrection);
    }
    dailyBalanceControl.removeCorrection(observableCorrection);
    dialogStage.close();
  }

  @FXML
  protected void doRemovePair(ActionEvent event) {
    if (observableCorrection.getPairedTransaction() != null) {
      observableCorrection.getPairedTransaction().removePairedCorrection(observableCorrection);
    }
    observableCorrection.setPairedTransaction(null);
    observableCorrection.setPairedTransactionId(null);
    //        pairedTransaction = null;
  }
}
