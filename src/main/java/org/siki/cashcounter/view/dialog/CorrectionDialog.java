package org.siki.cashcounter.view.dialog;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.converter.NumberStringConverter;
import lombok.Getter;
import org.siki.cashcounter.service.DataForViewService;
import org.siki.cashcounter.view.DailyBalanceControl;
import org.siki.cashcounter.view.model.ObservableAccountTransaction;
import org.siki.cashcounter.view.model.ObservableCorrection;

import static javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE;
import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;

public class CorrectionDialog extends Dialog<ButtonType> {
  @Getter private ObservableCorrection observableCorrection;
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

    this.initModality(Modality.APPLICATION_MODAL);
    this.initStyle(StageStyle.UTILITY);
    this.setTitle(dailyBalanceControl.getDate());

    ButtonType okButton = new ButtonType("Mentés", OK_DONE);
    ButtonType cancelButton = new ButtonType("Mégse", CANCEL_CLOSE);
    this.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);
  }

  private void prepareTable() {
    tblTransactions.setRowFactory(
        tv -> {
          TableRow<ObservableAccountTransaction> row = new TableRow<>();
          row.setOnMouseClicked(
              event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                  ObservableAccountTransaction rowData = row.getItem();
                  observableCorrection.amountProperty().set(rowData.getAmount());
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
    transactionTypeCol.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
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
    observableCorrection
        .amountProperty()
        .set(Integer.parseInt(tfAmount.getText().replaceAll("[^0-9\\-]", "")));
    observableCorrection.typeProperty().set(cbType.getValue());
    observableCorrection.commentProperty().set(tfComment.getText());

    this.close();
  }

  protected void doCancel(ActionEvent event) {
    this.close();
  }

  protected void doRemove(ActionEvent event) {
    if (observableCorrection.getPairedTransaction() != null) {
      observableCorrection.getPairedTransaction().removePairedCorrection(observableCorrection);
    }
    dailyBalanceControl.removeCorrection(observableCorrection);
    this.close();
  }

  protected void doRemovePair(ActionEvent event) {
    if (observableCorrection.getPairedTransaction() != null) {
      observableCorrection.getPairedTransaction().removePairedCorrection(observableCorrection);
    }
    observableCorrection.setPairedTransaction(null);
    observableCorrection.pairedTransactionIdProperty().set(0);
    //        pairedTransaction = null;
  }
}
