package org.siki.cashcounter.view.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.NoArgsConstructor;
import org.siki.cashcounter.model.DailyBalance;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class ObservableDailyBalance {
  private ObservableDailyBalance prevObservableDailyBalance;
  private ObjectProperty<LocalDate> dateProperty;
  private IntegerProperty balanceProperty;
  private BooleanProperty predictedProperty;
  private BooleanProperty reviewedProperty;
  private IntegerProperty dailySpendProperty;

  private ObservableList<ObservableSaving> observableSavings;
  private ObservableList<ObservableCorrection> observableCorrections;
  private ObservableList<ObservableAccountTransaction> observableTransactions;

  private DailyBalance dailyBalance;

  public ObjectProperty<LocalDate> dateProperty() {
    return dateProperty;
  }

  public String getDateString() {
    return dateProperty.get().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
  }

  public void setBalance(int value) {
    balanceProperty.set(value);
  }

  public void addToBalance(int value) {
    balanceProperty.set(balanceProperty.get() + value);
  }

  public IntegerProperty balanceProperty() {
    return balanceProperty;
  }

  public BooleanProperty predictedProperty() {
    return predictedProperty;
  }

  public BooleanProperty reviewedProperty() {
    return reviewedProperty;
  }

  public IntegerProperty dailySpendProperty() {
    return dailySpendProperty;
  }

  public ObservableList<ObservableCorrection> getObservableCorrections() {
    return observableCorrections;
  }

  public ObservableList<ObservableAccountTransaction> getObservableTransactions() {
    return observableTransactions;
  }

  public static ObservableDailyBalance of(DailyBalance dailyBalance) {
    var observableDailyBalance = new ObservableDailyBalance();
    observableDailyBalance.dailyBalance = dailyBalance;
    observableDailyBalance.dateProperty = new SimpleObjectProperty<>(dailyBalance.getDate());
    observableDailyBalance.balanceProperty = new SimpleIntegerProperty(dailyBalance.getBalance());
    observableDailyBalance.predictedProperty =
        new SimpleBooleanProperty(dailyBalance.isPredicted());
    observableDailyBalance.reviewedProperty = new SimpleBooleanProperty(dailyBalance.isReviewed());
    observableDailyBalance.dailySpendProperty =
        new SimpleIntegerProperty(dailyBalance.getDailySpend());
    observableDailyBalance.observableSavings =
        Optional.ofNullable(dailyBalance.getSavings())
            .map(
                s ->
                    s.stream()
                        .map(ObservableSaving::of)
                        .collect(Collectors.toCollection(FXCollections::observableArrayList)))
            .orElse(FXCollections.observableArrayList());
    observableDailyBalance.observableCorrections =
        dailyBalance.getCorrections().stream()
            .map(ObservableCorrection::of)
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
    observableDailyBalance.observableTransactions =
        dailyBalance.getTransactions().stream()
            .map(ObservableAccountTransaction::of)
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
    return observableDailyBalance;
  }

  public void addObservableCorrection(ObservableCorrection observableCorrection) {
    observableCorrections.add(observableCorrection);
    dailyBalance.addCorrection(observableCorrection.getCorrection());
    dailySpendProperty.set(dailyBalance.getDailySpend());
  }

  public void removeObservableCorrection(ObservableCorrection observableCorrection) {
    observableCorrections.remove(observableCorrection);
    dailyBalance.removeCorrection(observableCorrection.getCorrection());
    dailySpendProperty.set(dailyBalance.getDailySpend());
  }

  public void addObservableTransaction(ObservableAccountTransaction observableTransaction) {
    if (this.getObservableTransactions().stream().anyMatch(t -> t.similar(observableTransaction))) {
      observableTransaction.setPossibleDuplicate(true);
    }

    observableTransactions.add(observableTransaction);
    dailyBalance.addTransaction(observableTransaction.getAccountTransaction());
    addToBalance(observableTransaction.getAmount());
  }
}
