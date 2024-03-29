package org.siki.cashcounter.view.dailycorrections;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import lombok.Getter;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.model.DailyBalance;
import org.siki.cashcounter.view.ViewFactory;

import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public final class DailyBalanceControl extends VBox {
  @Getter private final DailyBalance dailyBalance;
  private final MonthlyBalanceTitledPane parent;
  private final ViewFactory viewFactory;

  @Getter
  private final ObservableList<CorrectionControl> correctionControls =
      FXCollections.observableArrayList();

  private CheckBox chkReviewed;
  private HBox hbCorrections;

  private Button btnAdd;
  private ToggleButton btnExpand;

  private TransactionListView transactionListView;

  public DailyBalanceControl(
      DailyBalance dailyBalance, MonthlyBalanceTitledPane parent, ViewFactory viewFactory) {
    this.dailyBalance = dailyBalance;
    this.parent = parent;
    this.viewFactory = viewFactory;

    setDragAndDrop();
    loadUI();
    setBackground();
    dailyBalance
        .getCorrections()
        .addListener(
            (ListChangeListener<Correction>)
                c -> {
                  while (c.next()) {
                    if (c.wasRemoved()) {
                      for (Correction removedItem : c.getRemoved()) {
                        removeCorrectionControl(removedItem);
                      }
                    } else if (c.wasAdded()) {
                      for (Correction addedItem : c.getAddedSubList()) {
                        addCorrectionControl(addedItem);
                      }
                    }
                  }
                });

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
            Correction data = (Correction) db.getContent(CorrectionControl.CORRECTION_DATA_FORMAT);
            data.setParentDailyBalance(dailyBalance);
            dailyBalance.addCorrection(data);
            dailyBalance.updateBalance();
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
    var currencyFormat = NumberFormat.getCurrencyInstance();
    currencyFormat.setMaximumFractionDigits(0);

    this.setMinHeight(40);
    this.setOnMouseEntered(this::mouseEntered);
    this.setOnMouseExited(this::mouseExited);
    this.setSpacing(0);

    var txtDate = new Label();
    txtDate.setPrefWidth(65);
    txtDate.disableProperty().bind(dailyBalance.predictedProperty());
    txtDate.setText(dailyBalance.getDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
    var txtBalance = new Label();
    txtBalance.setPrefWidth(80);
    txtBalance.setAlignment(Pos.CENTER_RIGHT);
    txtBalance.disableProperty().bind(dailyBalance.predictedProperty());
    txtBalance.textProperty().bindBidirectional(dailyBalance.balanceProperty(), currencyFormat);
    txtBalance.setOnMouseClicked(this::setBalanceManually);
    txtBalance
        .borderProperty()
        .bind(
            Bindings.createObjectBinding(
                () -> {
                  if (dailyBalance.getBalanceSetManually()) {
                    return new Border(
                        new BorderStroke(
                            Color.PURPLE,
                            BorderStrokeStyle.DASHED,
                            CornerRadii.EMPTY,
                            BorderWidths.DEFAULT));
                  } else {
                    return null;
                  }
                },
                dailyBalance.balanceSetManuallyProperty()));
    var txtDailySpend = new Label();
    txtDailySpend.setPrefWidth(70);
    txtDailySpend.setAlignment(Pos.CENTER_RIGHT);
    txtDailySpend.textProperty().bind(dailyBalance.unpairedDailySpentBinding);
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
    var bp = new BorderPane();
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

  private void setBalanceManually(MouseEvent event) {
    var currencyFormat = NumberFormat.getCurrencyInstance();
    currencyFormat.setMaximumFractionDigits(0);

    if (event.getClickCount() == 2) {
      var dialog =
          new TextInputDialog(
                  currencyFormat.format(dailyBalance.getBalance()));
      dialog.setHeaderText(dailyBalance.getDate().format(DateTimeFormatter.ISO_DATE));
      dialog.setContentText("Nap végi egyenleg:");
      dialog.initOwner(this.getScene().getWindow());
      dialog.initModality(Modality.APPLICATION_MODAL);
      var result = dialog.showAndWait();
      result.ifPresent(
          s -> {
            if (s.isEmpty()) {
              dailyBalance.setBalanceSetManually(false);
              dailyBalance.updateBalance();
            } else {
              dailyBalance.setBalance(Integer.parseInt(s.replaceAll("[^0-9]*", "")));
              dailyBalance.setBalanceSetManually(true);
            }
          });
    }
  }

  private void openAddCorrectionDialog(ActionEvent event) {
    var correctionDialog = viewFactory.createNewCorrectionDialog(this.getDailyBalance());
    correctionDialog.showAndWait();
  }

  public void addCorrectionControl(Correction correction) {
    if (correctionControls.stream()
        .anyMatch(cc -> cc.getCorrection().getId() == correction.getId())) {
      removeCorrectionControl(correction);
    }

    correctionControls.add(viewFactory.createCorrectionControl(correction, this));
  }

  public void removeCorrectionControl(Correction correction) {
    correctionControls.remove(
        correctionControls.stream()
            .filter(c -> c.getCorrection().getId() == correction.getId())
            .findFirst()
            .orElse(null));
  }

  private void mouseEntered(MouseEvent event) {
    btnAdd.setVisible(!chkReviewed.isSelected());
  }

  private void mouseExited(MouseEvent event) {
    btnAdd.setVisible(false);
  }

  public void validate() {
    this.isValid();
    this.parent.validate();
  }

  public void isValid() {
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
