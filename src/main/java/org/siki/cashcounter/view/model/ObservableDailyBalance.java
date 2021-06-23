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
  private ObservableList<ObservableTransaction> observableTransactions;

  private DailyBalance dailyBalance;

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

  public ObservableList<ObservableCorrection> getObservableCorrections() {
    return observableCorrections;
  }

  public ObservableList<ObservableTransaction> getObservableTransactions() {
    return observableTransactions;
  }

  public static ObservableDailyBalance of(DailyBalance dailyBalance) {
    var observableDailyBalance = new ObservableDailyBalance();
    observableDailyBalance.dailyBalance = dailyBalance;
    observableDailyBalance.dateProperty = new SimpleObjectProperty<>(dailyBalance.getDate());
    observableDailyBalance.balanceProperty = new SimpleIntegerProperty(dailyBalance.getBalance());
    observableDailyBalance.predictedProperty =
        new SimpleBooleanProperty(dailyBalance.getPredicted());
    observableDailyBalance.reviewedProperty = new SimpleBooleanProperty(dailyBalance.getReviewed());
    observableDailyBalance.dailySpendProperty =
        new SimpleIntegerProperty(dailyBalance.getUncoveredDailySpent());
    observableDailyBalance.observableSavings =
        Optional.ofNullable(dailyBalance.getSavings())
            .map(
                s ->
                    s.stream()
                        .map(ObservableSaving::of)
                        .collect(Collectors.toCollection(FXCollections::observableArrayList)))
            .orElse(FXCollections.observableArrayList());
    observableDailyBalance.observableTransactions =
        dailyBalance.getTransactions().stream()
            .map(ObservableTransaction::of)
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
    observableDailyBalance.observableCorrections =
        dailyBalance.getCorrections().stream()
            .map(c -> ObservableCorrection.of(c, observableDailyBalance))
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
    //    observableDailyBalance.observableTransactions.forEach(
    //        t ->
    //            observableDailyBalance.getObservableCorrections().stream()
    //                .filter(c -> c.getCorrection().getPairedTransactionId() == t.getId())
    //                .forEach(t::addPairedCorrection));

    return observableDailyBalance;
  }

  public void addObservableCorrection(ObservableCorrection observableCorrection) {
    observableCorrections.add(observableCorrection);
    dailyBalance.addCorrection(observableCorrection.getCorrection());
    setDailySpent(dailyBalance.getUncoveredDailySpent());
    setBalance(dailyBalance.getBalance());
  }

  public void removeObservableCorrection(ObservableCorrection observableCorrection) {
    observableCorrections.remove(observableCorrection);
    dailyBalance.removeCorrection(observableCorrection.getCorrection());
    setDailySpent(dailyBalance.getUncoveredDailySpent());
    setBalance(dailyBalance.getBalance());
  }

  public void addObservableTransaction(ObservableTransaction observableTransaction) {
    if (this.getObservableTransactions().stream().anyMatch(t -> t.similar(observableTransaction))) {
      observableTransaction.setPossibleDuplicate(true);
    }

    observableTransactions.add(observableTransaction);
    dailyBalance.addTransaction(observableTransaction.getAccountTransaction());
    addToBalance(observableTransaction.getAmount());
  }

  public int calculateDailySpent() {
    var transactionSum =
        observableTransactions.stream().mapToInt(ObservableTransaction::getAmount).sum();
    var notPairedCorrectionSum =
        observableCorrections.stream()
            .filter(ObservableCorrection::isNotPaired)
            .mapToInt(ObservableCorrection::getAmount)
            .sum();

    setDailySpent(transactionSum + notPairedCorrectionSum);
    return getDailySpent();
  }

  public int getTotalCorrections() {
    return observableCorrections.stream().mapToInt(ObservableCorrection::getAmount).sum();
  }
}
