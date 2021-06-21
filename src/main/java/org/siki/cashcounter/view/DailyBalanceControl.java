package org.siki.cashcounter.view;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import lombok.NoArgsConstructor;
import org.siki.cashcounter.model.DailyBalance;
import org.siki.cashcounter.view.model.ObservableDailyBalance;
import org.siki.cashcounter.view.model.ObservableSaving;
import org.siki.cashcounter.view.model.ObservableTransaction;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class DailyBalanceControl extends VBox {
  private MonthlyBalanceTitledPane parent;
  private ViewFactory viewFactory;

  private ObservableDailyBalance prevObservableDailyBalance;
  private ObjectProperty<LocalDate> dateProperty;
  private IntegerProperty balanceProperty;
  private BooleanProperty predictedProperty;
  private BooleanProperty reviewedProperty;
  private IntegerProperty dailySpendProperty;

  private ObservableList<ObservableSaving> observableSavings;
  @Getter private ObservableList<CorrectionControl> corrections;
  @Getter private ObservableList<ObservableTransaction> transactions;

  private Label txtDate;
  private Label txtBalance;
  private CheckBox chkReviewed;
  private HBox hbCorrections;

  private Button btnAdd;
  ToggleButton btnExpand;

  VBox vbTransactions = new VBox();

  private DailyBalance dailyBalance;
  private final ObservableList<CorrectionControl> correctionViewList =
      FXCollections.observableArrayList();

  public String getDateString() {
    return dateProperty.get().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
  }

  public LocalDate getDate() {
    return dateProperty.get();
  }

  public ObjectProperty<LocalDate> dateProperty() {
    return dateProperty;
  }

  public void setBalance(int value) {
    balanceProperty.set(value);
  }

  public void addToBalance(int value) {
    balanceProperty.set(balanceProperty.get() + value);
  }

  public int getBalance() {
    return balanceProperty.get();
  }

  public IntegerProperty balanceProperty() {
    return balanceProperty;
  }

  public BooleanProperty predictedProperty() {
    return predictedProperty;
  }

  public boolean isReviewed() {
    return reviewedProperty.get();
  }

  public boolean isNotReviewed() {
    return !isReviewed();
  }

  public BooleanProperty reviewedProperty() {
    return reviewedProperty;
  }

  public void setDailySpent(int value) {
    dailySpendProperty.set(value);
  }

  public int getDailySpent() {
    return dailySpendProperty.get();
  }

  public IntegerProperty dailySpendProperty() {
    return dailySpendProperty;
  }

  public static DailyBalanceControl of(
      DailyBalance dailyBalance, MonthlyBalanceTitledPane parent, ViewFactory viewFactory) {
    var dailyBalanceControl = new DailyBalanceControl();
    dailyBalanceControl.dailyBalance = dailyBalance;
    dailyBalanceControl.parent = parent;
    dailyBalanceControl.dateProperty = new SimpleObjectProperty<>(dailyBalance.getDate());
    dailyBalanceControl.balanceProperty = new SimpleIntegerProperty(dailyBalance.getBalance());
    dailyBalanceControl.predictedProperty = new SimpleBooleanProperty(dailyBalance.isPredicted());
    dailyBalanceControl.reviewedProperty = new SimpleBooleanProperty(dailyBalance.isReviewed());
    dailyBalanceControl.dailySpendProperty =
        new SimpleIntegerProperty(dailyBalance.getDailySpend());
    dailyBalanceControl.observableSavings =
        Optional.ofNullable(dailyBalance.getSavings())
            .map(
                s ->
                    s.stream()
                        .map(ObservableSaving::of)
                        .collect(Collectors.toCollection(FXCollections::observableArrayList)))
            .orElse(FXCollections.observableArrayList());
    dailyBalanceControl.transactions =
        dailyBalance.getTransactions().stream()
            .map(ObservableTransaction::of)
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
    dailyBalanceControl.corrections =
        dailyBalance.getCorrections().stream()
            .map(c -> viewFactory.createCorrectionControl(c, dailyBalanceControl))
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
    dailyBalanceControl.transactions.forEach(
        t ->
            dailyBalanceControl.getCorrections().stream()
                .filter(c -> c.getPairedTransactionId() == t.getId())
                .forEach(t::addPairedCorrection));

    dailyBalanceControl.setDragAndDrop();
    dailyBalanceControl.loadUI();
    dailyBalanceControl.loadCorrections();
    dailyBalanceControl.setBackground();

    return dailyBalanceControl;
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
            addCorrection(data);
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
    if (dateProperty().get().getDayOfWeek() == DayOfWeek.SATURDAY
        || dateProperty().get().getDayOfWeek() == DayOfWeek.SUNDAY) {
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

  public void loadCorrections() {
    corrections.forEach(
        correctionControl ->
            correctionViewList.add(
                viewFactory.createCorrectionControl(correctionControl.getCorrection(), this)));
  }

  private void loadUI() {
    NumberFormat currencyFormat = new DecimalFormat("#,###,###' Ft'");
    this.setMinHeight(40);
    this.setOnMouseEntered(this::mouseEntered);
    this.setOnMouseExited(this::mouseExited);
    this.setSpacing(0);

    var bp = new BorderPane();

    txtDate = new Label();
    txtDate.setPrefWidth(100);
    txtDate.disableProperty().bind(predictedProperty());
    txtDate.setText(getDateString());
    txtBalance = new Label();
    txtBalance.setPrefWidth(100);
    txtBalance.disableProperty().bind(predictedProperty());
    txtBalance.textProperty().bindBidirectional(balanceProperty(), currencyFormat);
    var txtDailySpend = new Label();
    txtDailySpend.setPrefWidth(100);
    txtDailySpend.textProperty().bindBidirectional(dailySpendProperty(), currencyFormat);
    btnAdd = new Button("+");
    btnAdd.onActionProperty().set(this::openAddCorrectionDialog);
    btnAdd.setVisible(false);

    hbCorrections = new HBox();
    hbCorrections.setSpacing(10);
    HBox.setMargin(hbCorrections, new Insets(0, 0, 0, 20));
    Bindings.bindContent(hbCorrections.getChildren(), correctionViewList);

    chkReviewed = new CheckBox();
    chkReviewed.visibleProperty().bind(predictedProperty().not());
    chkReviewed.selectedProperty().bindBidirectional(reviewedProperty());
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
          if (btnExpand.isSelected()) {
            if (vbTransactions.getChildren().isEmpty() && !transactions.isEmpty()) {
              vbTransactions
                  .getChildren()
                  .add(viewFactory.createTransactionControl(transactions, this));
            }
            this.getChildren().add(vbTransactions);
          } else {
            this.getChildren().remove(vbTransactions);
          }
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

  public void addCorrection(CorrectionControl correctionControl) {
    corrections.add(correctionControl);
    dailyBalance.addCorrection(correctionControl.getCorrection());
    setDailySpent(dailyBalance.getDailySpend());
    setBalance(dailyBalance.getBalance());
  }

  //  public void addCorrection(ObservableCorrection observableCorrection) {
  //    observableDailyBalance.addObservableCorrection(observableCorrection);
  //    correctionViewList.add(viewFactory.createCorrectionControl(observableCorrection, this));
  //  }

  public void removeCorrection(CorrectionControl correctionControl) {
    corrections.remove(
        corrections.stream().filter(c -> c.equals(correctionControl)).findFirst().orElse(null));
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
    boolean isValid = transactions.stream().allMatch(ObservableTransaction::isValid);

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
