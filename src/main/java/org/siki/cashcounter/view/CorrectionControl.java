package org.siki.cashcounter.view;

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
import javafx.util.converter.CurrencyStringConverter;
import org.siki.cashcounter.view.model.ObservableCorrection;

import static javafx.scene.layout.Priority.NEVER;

public class CorrectionControl extends GridPane {

  private static final StringProperty selectedCategoryProperty = new SimpleStringProperty();

  private Label txtType;
  private Text txtAmount;
  private Circle cirPaired;

  private final ObservableCorrection observableCorrection;
  private final DailyBalanceControl parent;

  public static final DataFormat CORRECTION_DATA_FORMAT =
      new DataFormat("com.siki.cashcount.model.Correction");

  public CorrectionControl(ObservableCorrection observableCorrection, DailyBalanceControl parent) {
    this.observableCorrection = observableCorrection;
    this.parent = parent;

    setDragAndDrop();
    loadUI();

    txtType.textProperty().bind(observableCorrection.commentProperty());
    var tt = new Tooltip();
    tt.textProperty().bind(txtType.textProperty());
    txtType.setTooltip(tt);
    txtAmount
        .textProperty()
        .bindBidirectional(observableCorrection.amountProperty(), new CurrencyStringConverter());
    cirPaired.visibleProperty().bind(observableCorrection.pairedProperty());

    selectedCategoryProperty.addListener(
        (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
          if (newValue.equals(observableCorrection.typeProperty().get())) {
            this.setStyle("-fx-background-color: yellow;");
          } else {
            this.setStyle("-fx-background-color: none;");
          }
        });
  }

  private void loadUI() {
    this.setMinWidth(100);
    this.setMaxWidth(100);
    GridPane.setHgrow(this, NEVER);
    var cc1 = new ColumnConstraints();
    cc1.setPrefWidth(100);
    this.getColumnConstraints().addAll(cc1, new ColumnConstraints());
    this.getRowConstraints().addAll(new RowConstraints(), new RowConstraints());
    txtType = new Label();
    GridPane.setColumnIndex(txtType, 0);
    GridPane.setRowIndex(txtType, 0);
    GridPane.setColumnSpan(txtType, 2);
    txtAmount = new Text();
    GridPane.setColumnIndex(txtAmount, 0);
    GridPane.setRowIndex(txtAmount, 1);
    cirPaired = new Circle();
    GridPane.setColumnIndex(cirPaired, 1);
    GridPane.setRowIndex(cirPaired, 1);
    cirPaired.setRadius(5);
    cirPaired.setFill(Color.BLUE);

    this.getChildren().addAll(txtType, txtAmount, cirPaired);
  }

  private void setDragAndDrop() {
    this.setOnDragDetected(
        (MouseEvent event) -> {
          /* drag was detected, start a drag-and-drop gesture*/
          /* allow any transfer mode */
          Dragboard db = this.startDragAndDrop(TransferMode.ANY);

          /* Put a string on a dragboard */
          var content = new ClipboardContent();
          content.put(CORRECTION_DATA_FORMAT, this.observableCorrection);
          db.setContent(content);

          event.consume();
        });
    this.setOnDragDone(
        (DragEvent event) -> {
          /* the drag and drop gesture ended */
          /* if the data was successfully moved, clear it */
          if (event.getTransferMode() == TransferMode.MOVE) {
            parent.removeCorrection(observableCorrection);
          }
          event.consume();
        });
  }

  public String getType() {
    return typeProperty().get();
  }

  public final void setType(String value) {
    typeProperty().set(value);
  }

  public StringProperty typeProperty() {
    return txtType.textProperty();
  }

  public String getAmount() {
    return amountProperty().get();
  }

  public final void setAmount(String value) {
    amountProperty().set(value);
  }

  public StringProperty amountProperty() {
    return txtAmount.textProperty();
  }
}
