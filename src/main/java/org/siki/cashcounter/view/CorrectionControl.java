package org.siki.cashcounter.view;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.service.CategoryService;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import static javafx.scene.layout.Priority.NEVER;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class CorrectionControl extends GridPane {
  public static final DataFormat CORRECTION_DATA_FORMAT =
      new DataFormat("com.siki.cashcounter.model.Correction");

  private CategoryService categoryService;
  private ViewFactory viewFactory;
  private DailyBalanceControl parent;

  @Getter private Correction correction;

  private IntegerProperty amountProperty;
  private StringProperty commentProperty;
  private StringProperty typeProperty;
  private ObjectProperty<AccountTransaction> pairedTransaction;

  public int getAmount() {
    return amountProperty.get();
  }

  public void setAmount(int value) {
    correction.setAmount(value);
    amountProperty.set(value);
  }

  public IntegerProperty amountProperty() {
    return amountProperty;
  }

  public void setComment(String value) {
    correction.setComment(value);
    commentProperty.set(value);
  }

  public StringProperty commentProperty() {
    return commentProperty;
  }

  public String getType() {
    return typeProperty.get();
  }

  public final void setType(String value) {
    typeProperty.set(value);
  }

  public StringProperty typeProperty() {
    return typeProperty;
  }

  public long getPairedTransactionId() {
    return correction.getPairedTransactionId();
  }

  public AccountTransaction getPairedTransaction() {
    return pairedTransaction.get();
  }

  public CorrectionControl(
      Correction correction,
      DailyBalanceControl parentDailyBalanceControl,
      CategoryService categoryService,
      ViewFactory viewFactory) {

    this.parent = parentDailyBalanceControl;
    this.categoryService = categoryService;
    this.correction = correction;
    this.viewFactory = viewFactory;

    this.amountProperty = new SimpleIntegerProperty();
    amountProperty.bind(correction.amountProperty());
    this.commentProperty = new SimpleStringProperty();
    commentProperty.bind(correction.commentProperty());
    this.typeProperty = new SimpleStringProperty();
    typeProperty.bind(correction.typeProperty());
    this.pairedTransaction =
        new SimpleObjectProperty<>(
            parentDailyBalanceControl.getDailyBalance().getTransactions().stream()
                .filter(t -> t.getId() == correction.getPairedTransactionId())
                .findFirst()
                .orElse(null));

    setDragAndDrop();
    loadUI();

    categoryService
        .selectedCategoryProperty()
        .addListener(
            (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
              if (newValue.equals(getType())) {
                setStyle("-fx-background-color: yellow;");
              } else {
                setStyle("-fx-background-color: none;");
              }
            });
  }

  private void loadUI() {
    NumberFormat currencyFormat = new DecimalFormat("#,###,###' Ft'");

    this.setMinWidth(100);
    this.setMaxWidth(100);
    GridPane.setHgrow(this, NEVER);
    var cc1 = new ColumnConstraints();
    cc1.setPrefWidth(100);
    this.getColumnConstraints().addAll(cc1, new ColumnConstraints());
    this.getRowConstraints().addAll(new RowConstraints(), new RowConstraints());
    var txtType = new Label();
    txtType.textProperty().bind(commentProperty);
    var tt = new Tooltip();
    tt.textProperty().bind(txtType.textProperty());
    txtType.setTooltip(tt);
    GridPane.setColumnIndex(txtType, 0);
    GridPane.setRowIndex(txtType, 0);
    GridPane.setColumnSpan(txtType, 2);
    var txtAmount = new Text();
    txtAmount.textProperty().bindBidirectional(amountProperty, currencyFormat);
    GridPane.setColumnIndex(txtAmount, 0);
    GridPane.setRowIndex(txtAmount, 1);
    var cirPaired = new Circle();
    GridPane.setColumnIndex(cirPaired, 1);
    GridPane.setRowIndex(cirPaired, 1);
    cirPaired.setRadius(5);
    cirPaired.setFill(Color.BLUE);
    cirPaired.visibleProperty().bind(correction.paired);

    this.getChildren().addAll(txtType, txtAmount, cirPaired);

    this.setOnMouseClicked(this::doModify);
  }

  private void setDragAndDrop() {
    this.setOnDragDetected(
        (MouseEvent event) -> {
          /* drag was detected, start a drag-and-drop gesture*/
          /* allow any transfer mode */
          Dragboard db = this.startDragAndDrop(TransferMode.ANY);

          /* Put a string on a dragboard */
          var content = new ClipboardContent();
          content.put(CORRECTION_DATA_FORMAT, this.correction);
          db.setContent(content);

          event.consume();
        });
    this.setOnDragDone(
        (DragEvent event) -> {
          /* the drag and drop gesture ended */
          /* if the data was successfully moved, clear it */
          if (event.getTransferMode() == TransferMode.MOVE) {
            parent.getDailyBalance().removeCorrection(correction);
          }
          event.consume();
        });
  }

  public void doModify(MouseEvent event) {
    if (event.getClickCount() == 1) {
      if (getType().equals(categoryService.getSelectedCategory())) {
        categoryService.setSelectedCategory("");
      } else {
        categoryService.setSelectedCategory(getType());
      }
    } else if (event.getClickCount() == 2) {
      var correctionDialog =
          viewFactory.editCorrectionDialog(this.correction, parent.getDailyBalance());
      correctionDialog.showAndWait();
    }
  }
}
