package org.siki.cashcounter.view;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.service.DataForViewService;

import java.text.NumberFormat;

public class TransactionListView extends GridPane {
  private final ObservableList<AccountTransaction> transactions;
  private final DailyBalanceControl parent;
  private final DataForViewService dataForViewService;

  public TransactionListView(
      ObservableList<AccountTransaction> transactions,
      DailyBalanceControl parent,
      DataForViewService dataForViewService) {
    this.transactions = transactions;
    this.parent = parent;
    this.dataForViewService = dataForViewService;

    buildLayout();
    transactions.addListener((ListChangeListener<AccountTransaction>) c -> buildLayout());
  }

  private void buildLayout() {
    var currencyFormat = NumberFormat.getCurrencyInstance();
    currencyFormat.setMaximumFractionDigits(0);

    this.getChildren().clear();
    int rowCnt = -1;
    for (AccountTransaction t : transactions) {
      rowCnt++;
      var lblType = new Label(t.getType());
      var lblAmount = new Label(currencyFormat.format(t.getAmount()));
      var lblOwner = new SelectableLabel(t.getOwner());
      var isPaired = new Circle(10, new Color(0, 0, 1, 1));
      isPaired.visibleProperty().bind(t.pairedProperty());
      var lblComment = new SelectableLabel(t.getComment());

      GridPane.setConstraints(lblType, 0, rowCnt);
      GridPane.setConstraints(lblAmount, 1, rowCnt);
      GridPane.setConstraints(lblOwner, 2, rowCnt);
      GridPane.setConstraints(isPaired, 3, rowCnt);
      GridPane.setConstraints(lblComment, 4, rowCnt);

      this.getChildren().addAll(lblType, lblAmount, lblOwner, isPaired, lblComment);
      addCategoryPicker(t, rowCnt);

      this.managedProperty().bind(this.visibleProperty());
    }

    this.setStyle("-fx-background-color: white;");
    this.setHgap(20);
    validate();
  }

  private void addCategoryPicker(AccountTransaction transaction, int rowCnt) {
    //    if (transaction.getUnpairedAmount() != 0) {
    var cbCategory = new ComboBox<String>();
    cbCategory.setEditable(true);
    cbCategory.setItems(dataForViewService.getAllCategories());
    cbCategory.valueProperty().bindBidirectional(transaction.categoryProperty());
    cbCategory.setPrefWidth(200);
    //       Unpaired amount not counted
    //      cbCategory.visibleProperty().bind(transaction.pairedProperty().not());
    cbCategory.valueProperty().addListener((observable, oldValue, newValue) -> validate());

    GridPane.setConstraints(cbCategory, 6, rowCnt);
    this.getChildren().add(cbCategory);
    //    }
  }

  private void validate() {
    this.isValid();
    this.parent.validate();
  }

  public void isValid() {
    boolean isValid = transactions.stream().allMatch(AccountTransaction::isValid);

    if (!isValid) {
      this.setBorder(
          new Border(
              new BorderStroke(
                  Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    } else {
      this.setBorder(
          new Border(
              new BorderStroke(
                  Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    }
  }
}
