package org.siki.cashcounter.view;

import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.siki.cashcounter.service.DataForViewService;
import org.siki.cashcounter.view.model.ObservableTransaction;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class TransactionControl extends GridPane {
  private final ObservableList<ObservableTransaction> observableTransactions;
  private final DailyBalanceControl parent;
  private final DataForViewService dataForViewService;

  public TransactionControl(
      ObservableList<ObservableTransaction> observableTransactions,
      DailyBalanceControl parent,
      DataForViewService dataForViewService) {
    this.observableTransactions = observableTransactions;
    this.parent = parent;
    this.dataForViewService = dataForViewService;

    buildLayout();
  }

  private void buildLayout() {
    NumberFormat currencyFormat = new DecimalFormat("#,###,###' Ft'");

    this.getChildren().clear();
    int rowCnt = -1;
    for (ObservableTransaction t : observableTransactions) {
      rowCnt++;
      var lblType = new Label(t.getType());
      var lblAmount = new Label(currencyFormat.format(t.getAmount()));
      var lblOwner = new Label(t.getOwner());
      var isPaired = new Circle(10, new Color(0, 0, 1, 1));
      isPaired.visibleProperty().bind(t.pairedProperty());
      var lblComment = new Label(t.getComment());

      GridPane.setConstraints(lblType, 0, rowCnt);
      GridPane.setConstraints(lblAmount, 1, rowCnt);
      GridPane.setConstraints(lblOwner, 2, rowCnt);
      GridPane.setConstraints(isPaired, 3, rowCnt);
      GridPane.setConstraints(lblComment, 4, rowCnt);

      if (t.isPossibleDuplicate()) {
        var duplicateHandler = new HBox();
        var removeDuplicateButton = new Button("töröl");
        removeDuplicateButton.setOnAction(
            event -> {
              this.observableTransactions.remove(t);
              buildLayout();
            });
        var notDuplicateButton = new Button("hozzáad");
        notDuplicateButton.setOnAction(
            event -> {
              t.setPossibleDuplicate(false);
              buildLayout();
            });
        duplicateHandler.getChildren().addAll(removeDuplicateButton, notDuplicateButton);
        GridPane.setConstraints(duplicateHandler, 5, rowCnt);
        this.getChildren().add(duplicateHandler);
      }

      this.getChildren().addAll(lblType, lblAmount, lblOwner, isPaired, lblComment);
      addCategoryPicker(t, rowCnt);
    }

    this.setStyle("-fx-background-color: white;");
    this.setHgap(20);
    validate();
  }

  private void addCategoryPicker(ObservableTransaction observableTransaction, int rowCnt) {
    ComboBox cbCategory = null;
    if (observableTransaction.getNotPairedAmount() != 0) {
      cbCategory = new ComboBox();
      cbCategory.setEditable(true);
      cbCategory.setItems(dataForViewService.getAllCategories());
      cbCategory.valueProperty().bindBidirectional(observableTransaction.categoryProperty());
      cbCategory.setPrefWidth(200);
      cbCategory
          .valueProperty()
          .addListener((ChangeListener<String>) (observable, oldValue, newValue) -> validate());

      GridPane.setConstraints(cbCategory, 6, rowCnt);
      this.getChildren().add(cbCategory);
    }
  }

  private void validate() {
    this.isValid();
    this.parent.validate();
  }

  public void isValid() {
    boolean isValid = observableTransactions.stream().allMatch(ObservableTransaction::isValid);

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
