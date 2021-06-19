package org.siki.cashcounter.view;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.Getter;
import org.siki.cashcounter.view.model.ObservableAccountTransaction;
import org.siki.cashcounter.view.model.ObservableCorrection;
import org.siki.cashcounter.view.model.ObservableDailyBalance;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.DayOfWeek;

public final class DailyBalanceControl extends VBox {
  private final MonthlyBalanceTitledPane parent;
  private final ViewFactory viewFactory;

  private Label txtDate;
  private Label txtBalance;
  private Label txtDailySpend;
  private CheckBox chkReviewed;
  private HBox hbCorrections;
  private HBox hbLine;

  private Button btnAdd;
  ToggleButton btnExpand;

  VBox vbTransactions = new VBox();

  @Getter private final ObservableDailyBalance observableDailyBalance;

  DailyBalanceControl(
      ObservableDailyBalance observableDailyBalance,
      MonthlyBalanceTitledPane parent,
      ViewFactory viewFactory) {
    this.observableDailyBalance = observableDailyBalance;
    this.parent = parent;
    this.viewFactory = viewFactory;

    setDragAndDrop();

    loadUI();

    btnAdd.onActionProperty().set(this::addCorrection);
    btnAdd.setVisible(false);
    chkReviewed.visibleProperty().bind(observableDailyBalance.predictedProperty().not());

    txtBalance.disableProperty().bind(observableDailyBalance.predictedProperty());
    txtDate.disableProperty().bind(observableDailyBalance.predictedProperty());

    setDate(observableDailyBalance.getDateString());
    NumberFormat currencyFormat = new DecimalFormat("#,###,###' Ft'");
    txtBalance
        .textProperty()
        .bindBidirectional(this.observableDailyBalance.balanceProperty(), currencyFormat);
    txtDailySpend
        .textProperty()
        .bindBidirectional(this.observableDailyBalance.dailySpendProperty(), currencyFormat);

    chkReviewed.selectedProperty().bindBidirectional(observableDailyBalance.reviewedProperty());
    hbLine.disableProperty().bind(chkReviewed.selectedProperty());
    chkReviewed
        .selectedProperty()
        .addListener(
            (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) ->
                setBackground());
    loadCorrections();
    observableDailyBalance
        .getObservableCorrections()
        .removeListener((ListChangeListener<ObservableCorrection>) c -> loadCorrections());

    setBackground();
  }

  private void setDragAndDrop() {
    this.setOnDragOver(
        (DragEvent event) -> {
          /* data is dragged over the target */
          /* accept it only if it is not dragged from the same node
           * and if it has a string data */
          if (((CorrectionControl) event.getGestureSource()).getParent() != hbCorrections
              && !chkReviewed.isSelected()
              && event.getDragboard().hasContent(CorrectionControl.CORRECTION_DATA_FORMAT)) {
            /* allow for moving */
            event.acceptTransferModes(TransferMode.MOVE);
          }

          event.consume();
        });
    this.setOnDragEntered(
        (DragEvent event) -> {
          /* the drag-and-drop gesture entered the target */
          /* show to the user that it is an actual gesture target */
          if (((CorrectionControl) event.getGestureSource()).getParent() != hbCorrections
              && !chkReviewed.isSelected()
              && event.getDragboard().hasContent(CorrectionControl.CORRECTION_DATA_FORMAT)) {
            this.setStyle("-fx-background-color: yellow;");
          }

          event.consume();
        });
    this.setOnDragExited(
        (DragEvent event) -> {
          /* mouse moved away, remove the graphical cues */
          setBackground();

          event.consume();
        });
    this.setOnDragDropped(
        (DragEvent event) -> {
          /* data dropped */
          /* if there is a string data on dragboard, read it and use it */
          var db = event.getDragboard();
          var success = false;
          if (db.hasContent(CorrectionControl.CORRECTION_DATA_FORMAT)) {
            ObservableCorrection data =
                (ObservableCorrection) db.getContent(CorrectionControl.CORRECTION_DATA_FORMAT);
            observableDailyBalance.addObservableCorrection(data);
            loadCorrections();
            success = true;
          }
          /* let the source know whether the string was successfully
           * transferred and used */
          event.setDropCompleted(success);

          event.consume();
        });
  }

  private void setBackground() {
    if (observableDailyBalance.dateProperty().get().getDayOfWeek() == DayOfWeek.SATURDAY
        || observableDailyBalance.dateProperty().get().getDayOfWeek() == DayOfWeek.SUNDAY) {
      if (chkReviewed.isSelected()) {
        this.setStyle("-fx-background-color: green;");
      } else {
        this.setStyle("-fx-background-color: lightgrey;");
      }
    } else if (chkReviewed.isSelected()) {
      this.setStyle("-fx-background-color: lightgreen;");
    } else {
      this.setStyle("-fx-background-color: none;");
    }
  }

  // TODO: bind correction list to bhCorrections instead
  public void loadCorrections() {
    hbCorrections.getChildren().clear();
    observableDailyBalance
        .getObservableCorrections()
        .forEach(
            observableCorrection ->
                hbCorrections
                    .getChildren()
                    .add(viewFactory.createCorrectionControl(observableCorrection, this)));
  }

  private void loadUI() {
    this.setMinHeight(40);
    this.setOnMouseEntered(this::mouseEntered);
    this.setOnMouseExited(this::mouseExited);
    this.setSpacing(0);

    var bp = new BorderPane();

    txtDate = new Label();
    txtDate.setPrefWidth(100);
    txtBalance = new Label();
    txtBalance.setPrefWidth(100);
    txtDailySpend = new Label();
    txtDailySpend.setPrefWidth(100);
    btnAdd = new Button("+");

    hbCorrections = new HBox();
    hbCorrections.setSpacing(10);
    HBox.setMargin(hbCorrections, new Insets(0, 0, 0, 20));

    hbLine = new HBox();
    hbLine.getChildren().addAll(txtDate, txtBalance, txtDailySpend, btnAdd, hbCorrections);
    bp.setCenter(hbLine);

    var rightContext = new HBox();

    btnExpand = new ToggleButton("...");
    btnExpand.setOnAction(
        event -> {
          if (btnExpand.isSelected()) {
            if (vbTransactions.getChildren().isEmpty()
                && !observableDailyBalance.getObservableTransactions().isEmpty()) {
              vbTransactions
                  .getChildren()
                  .add(
                      viewFactory.createTransactionControl(
                          observableDailyBalance.getObservableTransactions(), this));
            }
            this.getChildren().add(vbTransactions);
          } else {
            this.getChildren().remove(vbTransactions);
          }
        });

    chkReviewed = new CheckBox();
    rightContext.getChildren().addAll(chkReviewed, btnExpand);
    bp.setRight(rightContext);

    this.getChildren().addAll(bp);
    validate();
  }

  public String getDate() {
    return dateProperty().get();
  }

  public final void setDate(String value) {
    dateProperty().set(value);
  }

  public StringProperty dateProperty() {
    return txtDate.textProperty();
  }

  public String getBalance() {
    return balanceProperty().get();
  }

  public final void setBalance(String value) {
    balanceProperty().set(value);
  }

  public StringProperty balanceProperty() {
    return txtBalance.textProperty();
  }

  protected void addCorrection(ActionEvent event) {
    var correctionDialog = viewFactory.createNewCorrectionDialog(observableDailyBalance);
    correctionDialog.showAndWait();
  }

  public void removeCorrection(ObservableCorrection observableCorrection) {
    observableDailyBalance.removeObservableCorrection(observableCorrection);
    loadCorrections();
    //      DataManager.getInstance().calculatePredictions();
  }

  private void mouseEntered(MouseEvent event) {
    btnAdd.setVisible(!chkReviewed.isSelected());
  }

  private void mouseExited(MouseEvent event) {
    btnAdd.setVisible(false);
  }

  void validate() {
    this.isValid();
    this.parent.validate();
  }

  void isValid() {
    boolean isValid =
        observableDailyBalance.getObservableTransactions().stream()
            .allMatch(ObservableAccountTransaction::isValid);

    if (!isValid) {
      this.setBorder(
          new Border(
              new BorderStroke(
                  Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    } else {
      this.setBorder(
          new Border(
              new BorderStroke(
                  Color.TRANSPARENT,
                  BorderStrokeStyle.SOLID,
                  CornerRadii.EMPTY,
                  BorderWidths.DEFAULT)));
    }

    if (btnExpand.isSelected()) {
      for (Node child : vbTransactions.getChildren()) {
        if (child.getClass() == TransactionControl.class) {
          ((TransactionControl) child).isValid();
        }
      }
    }
  }
}
