package org.siki.cashcounter.view.dialog;

import com.siki.cashcount.data.DataManager;
import com.siki.cashcount.model.AccountTransaction;
import com.siki.cashcount.model.Correction;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.NumberStringConverter;
import org.siki.cashcounter.view.DailyBalanceControl;
import org.siki.cashcounter.view.model.ObservableAccountTransaction;
import org.siki.cashcounter.view.model.ObservableCorrection;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CorrectionDialog extends Dialog<ObservableCorrection> {
    private ObservableCorrection observableCorrection;
    private DailyBalanceControl dailyBalanceControl;
//    AccountTransaction pairedTransaction;
    
    private Stage dialogStage;
    private boolean okClicked = false;
    
    ComboBox<String> cbType;
    TextField tfAmount;
    TextField tfComment;
    TableView<ObservableAccountTransaction> tblTransactions;

    public CorrectionDialog(Parent parent) {
        var lblType = new Label("Típus");
        cbType = new ComboBox<>();
        cbType.setEditable(true);
        var lblAmount = new Label("Összeg");
        tfAmount = new TextField();
        var lblComment = new Label("Megjegyzés");
        tfComment = new TextField();
        var btnRemovePair = new Button("Párosítás törlése");
        btnRemovePair.setOnAction(this::doRemovePair);
        var grid = new GridPane();
        grid.getColumnConstraints().addAll(new ColumnConstraints(), new ColumnConstraints(), new ColumnConstraints());
        grid.getRowConstraints().addAll(new RowConstraints(), new RowConstraints(), new RowConstraints());
        grid.getChildren().addAll(lblType, cbType, lblAmount, tfAmount, lblComment, tfComment, btnRemovePair);
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
        var scene = new Scene(parent,)

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tfAmount.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));
            
        cbType.setItems(DataManager.getInstance().getAllCorrectionTypes());
    }    
    
    public void setContext(Correction correction, DailyBalanceControl dbControl) {
        cbType.setValue(correction.getType());
        tfAmount.setText(correction.getAmount().toString());
        tfComment.setText(correction.getComment());
        
        this.observableCorrection = correction;
        this.dailyBalanceControl = dbControl;
//        this.pairedTransaction = correction.getPairedTransaction();
        
        prepareTable();
        try {
            tblTransactions.setItems(dbControl.getDailyBalance().getTransactions());
        } catch (Exception ex) {
            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public boolean isOkClicked() {
        return okClicked;
    }
    
    private void prepareTable() {     
        tblTransactions.setRowFactory( tv -> {
            TableRow<AccountTransaction> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    AccountTransaction rowData = row.getItem();
                    observableCorrection.setAmount(rowData.getAmount());
                    tfAmount.setText(rowData.getAmount().toString());
                    if (observableCorrection.getPairedTransaction() != null && observableCorrection.getPairedTransaction() != rowData) {
                        observableCorrection.getPairedTransaction().removePairedCorrection(observableCorrection);
                    }
                    if (rowData != null && observableCorrection.getPairedTransaction() != rowData) {
                        rowData.addPairedCorrection(observableCorrection);
                        observableCorrection.setPairedTransaction(rowData);
                    } else {
                        observableCorrection.setPairedTransaction(null);
                    }
                }
            });
            return row ;
        });
        
        TableColumn<AccountTransaction, String> transactionTypeCol = new TableColumn<>("Forgalom típusa");
        transactionTypeCol.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
//        TableColumn<AccountTransaction, LocalDate> dateCol = new TableColumn<>("Könyvelési dátum");
//        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<AccountTransaction, Integer> amountCol = new TableColumn<>("Összeg");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
//        TableColumn<AccountTransaction, Integer> balanceCol = new TableColumn<>("Új könyvelt egyenleg");
//        balanceCol.setCellValueFactory(new PropertyValueFactory<>("balance"));
//        TableColumn<AccountTransaction, String> accountNumberCol = new TableColumn<>("Ellenoldali számlaszám");
//        accountNumberCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        TableColumn<AccountTransaction, String> ownerCol = new TableColumn<>("Ellenoldali név");
        ownerCol.setCellValueFactory(new PropertyValueFactory<>("owner"));
        TableColumn<AccountTransaction, String> commentCol = new TableColumn<>("Közlemény");
        commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));
//        TableColumn<AccountTransaction, String> counterCol = new TableColumn<>("?");
//        counterCol.setCellValueFactory(new PropertyValueFactory<>("counter"));    
        TableColumn<AccountTransaction, Boolean> isPairedCol = new TableColumn<>("Párosítva");
        isPairedCol.setCellValueFactory(new PropertyValueFactory<>("paired"));
        isPairedCol.setCellFactory(new Callback<TableColumn<AccountTransaction, Boolean>, TableCell<AccountTransaction, Boolean>>() {
            @Override
            public TableCell<AccountTransaction, Boolean> call(TableColumn<AccountTransaction, Boolean> param) {
                return new TableCell<AccountTransaction, Boolean>(){
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
        tblTransactions.getColumns().setAll(transactionTypeCol, amountCol, isPairedCol, ownerCol, commentCol);    
    }    
    
    @FXML
    protected void doSave(ActionEvent event) {
        observableCorrection.setAmount(Integer.parseInt(tfAmount.getText().replaceAll("[^0-9\\-]", "")));
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
