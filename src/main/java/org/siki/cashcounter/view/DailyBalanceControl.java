package org.siki.cashcounter.view;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.model.DailyBalance;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public final class DailyBalanceControl extends VBox {
  @Getter private final DailyBalance dailyBalance;
  private final MonthlyBalanceTitledPane parent;
  private final ViewFactory viewFactory;

  private DailyBalanceControl prevDailyBalanceControl;

  @Getter
  private final ObservableList<CorrectionControl> correctionControls =
      FXCollections.observableArrayList();

  private CheckBox chkReviewed;
  private HBox hbCorrections;

  private Button btnAdd;
  ToggleButton btnExpand;

  //  VBox vbTransactions = new VBox();
  TransactionListView transactionListView;

  public DailyBalanceControl(
      DailyBalance dailyBalance, MonthlyBalanceTitledPane parent, ViewFactory viewFactory) {
    this.dailyBalance = dailyBalance;
    this.parent = parent;
    this.viewFactory = viewFactory;

    setDragAndDrop();
    loadUI();
    setBackground();

    this.correctionControls.addAll(
        dailyBalance.getCorrections().stream()
            .map(c -> viewFactory.createCorrectionControl(c, this))
            .collect(Collectors.toCollection(FXCollections::observableArrayList)));
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
            CorrectionControl data =
                (CorrectionControl) db.getContent(CorrectionControl.CORRECTION_DATA_FORMAT);
            addCorrection(data.getCorrection());
            success = true;
          }
          /* let the source know whether the string was successfully
           * transferred and used */
          event.setDropCompleted(success);

          event.consume();
        });
  }

  private void setBackground() {
    if (dailyBalance.getDate().getDayOfWeek() == DayOfWeek.SATURDAY
        || dailyBalance.getDate().getDayOfWeek() == DayOfWeek.SUNDAY) {
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

  private void loadUI() {
    NumberFormat currencyFormat = new DecimalFormat("#,###,###' Ft'");
    this.setMinHeight(40);
    this.setOnMouseEntered(this::mouseEntered);
    this.setOnMouseExited(this::mouseExited);
    this.setSpacing(0);

    var bp = new BorderPane();

    var txtDate = new Label();
    txtDate.setPrefWidth(100);
    txtDate.disableProperty().bind(dailyBalance.predictedProperty());
    txtDate.setText(dailyBalance.getDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
    var txtBalance = new Label();
    txtBalance.setPrefWidth(100);
    txtBalance.disableProperty().bind(dailyBalance.predictedProperty());
    txtBalance.textProperty().bindBidirectional(dailyBalance.balanceProperty(), currencyFormat);
    var txtDailySpend = new Label();
    txtDailySpend.setPrefWidth(100);
    txtDailySpend.textProperty().bind(dailyBalance.notPairedDailySpent);
    btnAdd = new Button("+");
    btnAdd.onActionProperty().set(this::openAddCorrectionDialog);
    btnAdd.setVisible(false);

    hbCorrections = new HBox();
    hbCorrections.setSpacing(10);
    HBox.setMargin(hbCorrections, new Insets(0, 0, 0, 20));
    Bindings.bindContent(hbCorrections.getChildren(), correctionControls);

    chkReviewed = new CheckBox();
    chkReviewed.visibleProperty().bind(dailyBalance.predictedProperty().not());
    chkReviewed.selectedProperty().bindBidirectional(dailyBalance.reviewedProperty());
    chkReviewed
        .selectedProperty()
        .addListener(
            (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) ->
                setBackground());

    var hbLine = new HBox();
    hbLine.disableProperty().bind(chkReviewed.selectedProperty());
    hbLine.getChildren().addAll(txtDate, txtBalance, txtDailySpend, btnAdd, hbCorrections);
    bp.setCenter(hbLine);

    var rightContext = new HBox();

    btnExpand = new ToggleButton("...");
    btnExpand.setOnAction(
        event -> {
          if (transactionListView == null) {
            transactionListView =
                viewFactory.createTransactionListView(dailyBalance.getTransactions(), this);
            this.getChildren().add(transactionListView);
          }

          transactionListView.setVisible(btnExpand.isSelected());
        });

    rightContext.getChildren().addAll(chkReviewed, btnExpand);
    bp.setRight(rightContext);

    this.getChildren().addAll(bp);
    validate();
  }

  protected void openAddCorrectionDialog(ActionEvent event) {
    var correctionDialog = viewFactory.createNewCorrectionDialog(this);
    correctionDialog.showAndWait();
  }

  public void addCorrection(Correction correction) {
    correctionControls.add(viewFactory.createCorrectionControl(correction, this));
    dailyBalance.addCorrection(correction);
  }

  public void removeCorrection(Correction correction) {
    correctionControls.remove(
        correctionControls.stream()
            .filter(c -> c.getCorrection().getId() == correction.getId())
            .findFirst()
            .orElse(null));
  }

  public void addTransaction(AccountTransaction transaction) {
    if (dailyBalance.getTransactions().stream().anyMatch(t -> t.similar(transaction))) {
      transaction.setPossibleDuplicate(true);
    }

    dailyBalance.addTransaction(transaction);
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
    boolean isValid = dailyBalance.getTransactions().stream().allMatch(AccountTransaction::isValid);

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
      for (Node child : this.getChildren()) {
        if (child.getClass() == TransactionListView.class) {
          ((TransactionListView) child).isValid();
        }
      }
    }
  }
}
