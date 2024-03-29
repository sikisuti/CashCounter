package org.siki.cashcounter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.Setter;
import org.siki.cashcounter.model.converter.CorrectionListToObservableConverter;
import org.siki.cashcounter.model.converter.TransactionListToObservableConverter;
import org.siki.cashcounter.repository.DataManager;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(converter = DailyBalance.PostConstruct.class)
public final class DailyBalance {
  @Setter private DataManager dataManager;

  private final ObjectProperty<LocalDate> date;
  private final IntegerProperty balance;
  private final BooleanProperty balanceSetManually;
  private final BooleanProperty predicted;
  private final BooleanProperty reviewed;
  @JsonIgnore public StringBinding unpairedDailySpentBinding;
  @JsonIgnore public IntegerBinding dayAverageBinding;
  @JsonIgnore public IntegerBinding balanceWithSaving;
  @JsonIgnore public IntegerBinding dailySavings;

  @Setter private DailyBalance prevDailyBalance;

  @Getter @Setter @JsonIgnore private ObservableList<Saving> savings;

  @Getter
  @Setter
  @JsonDeserialize(converter = CorrectionListToObservableConverter.class)
  private ObservableList<Correction> corrections;

  @Getter
  @Setter
  @JsonDeserialize(converter = TransactionListToObservableConverter.class)
  private ObservableList<AccountTransaction> transactions;

  public LocalDate getDate() {
    return date.get();
  }

  public void setDate(LocalDate value) {
    date.set(value);
  }

  public ObjectProperty<LocalDate> dateProperty() {
    return date;
  }

  public int getBalance() {
    return balance.get();
  }

  public void setBalance(int value) {
    balance.set(value);
  }

  public void addBalance(int value) {
    balance.set(balance.get() + value);
  }

  public void minusBalance(int value) {
    balance.set(balance.get() - value);
  }

  public IntegerProperty balanceProperty() {
    return balance;
  }

  public boolean getBalanceSetManually() {
    return balanceSetManually.get();
  }

  public void setBalanceSetManually(boolean value) {
    balanceSetManually.set(value);
  }

  public BooleanProperty balanceSetManuallyProperty() {
    return balanceSetManually;
  }

  public boolean getPredicted() {
    return predicted.get();
  }

  public void setPredicted(boolean value) {
    predicted.set(value);
  }

  public BooleanProperty predictedProperty() {
    return predicted;
  }

  public boolean getReviewed() {
    return reviewed.get();
  }

  @JsonIgnore
  public boolean isNotReviewed() {
    return !getReviewed();
  }

  public void setReviewed(boolean value) {
    reviewed.set(value);
  }

  public BooleanProperty reviewedProperty() {
    return reviewed;
  }

  @JsonIgnore
  public int getUnpairedDailySpent() {
    return transactions.stream().mapToInt(AccountTransaction::getUnpairedAmount).sum();
  }

  public DailyBalance() {
    date = new SimpleObjectProperty<>();
    balance = new SimpleIntegerProperty();
    balanceSetManually = new SimpleBooleanProperty();
    predicted = new SimpleBooleanProperty();
    reviewed = new SimpleBooleanProperty();

    savings = FXCollections.observableArrayList();
    corrections = FXCollections.observableArrayList();
    transactions = FXCollections.observableArrayList();
  }

  public void addSaving(Saving saving) {
    savings.add(saving);
  }

  @JsonIgnore
  public Integer getTotalSavings() {
    return savings.stream().mapToInt(Saving::getAmount).sum();
  }

  public void addCorrection(Correction correction) {
    if (corrections.stream().noneMatch(c -> c.getId() == correction.getId())) {
      corrections.add(correction);
    }

    unpairedDailySpentBinding.invalidate();
  }

  public void removeCorrection(Correction correction) {
    var isRemoved = corrections.remove(correction);
    if (isRemoved) {
      if (correction.paired.get()) {
        transactions.stream()
            .filter(t -> t.getId() == correction.getPairedTransactionId())
            .findFirst()
            .ifPresent(t -> t.removePairedCorrection(correction));
      }

      unpairedDailySpentBinding.invalidate();
      updateBalance();
    }
  }

  public void addTransaction(AccountTransaction transaction) {
    transactions.add(transaction);
    setBalance(balance.get() + transaction.getAmount());
  }

  public int addTransactions(List<AccountTransaction> transactionsToAdd) {
    var counter = 0;
    for (var transaction : transactionsToAdd) {
      if (transactions.stream().noneMatch(t -> t.similar(transaction))) {
        addTransaction(transaction);
        setReviewed(false);
        counter++;
      }
    }

    return counter;
  }

  @JsonIgnore
  public Optional<AccountTransaction> getTransactionById(long id) {
    return transactions.stream().filter(t -> t.getId() == id).findFirst();
  }

  public void updateBalance() {
    if (!getBalanceSetManually()) {
      int newBalance = prevDailyBalance.getBalance() + getAllDailySpent();
      if (isNotReviewed()) {
        newBalance += dayAverageBinding.get();
      }

      setBalance(newBalance);
    }
  }

  @JsonIgnore
  public int getAllDailySpent() {
    var transactionSum = transactions.stream().mapToInt(AccountTransaction::getAmount).sum();
    var notPairedCorrectionSum =
        corrections.stream()
            .filter(c -> c.paired.not().get())
            .mapToInt(Correction::getAmount)
            .sum();

    return transactionSum + notPairedCorrectionSum;
  }

  @JsonIgnore
  public Integer getTotalCorrections() {
    return corrections.stream().mapToInt(Correction::getAmount).sum();
  }

  @JsonIgnore
  public boolean isValid() {
    return transactions.stream().allMatch(AccountTransaction::isValid);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    DailyBalance other;
    if (obj instanceof DailyBalance) {
      other = (DailyBalance) obj;
    } else {
      return false;
    }

    boolean rtn =
        this.getDate().equals(other.getDate())
            && this.getBalance() == other.getBalance()
            && this.predicted == other.predicted
            && this.reviewed == other.reviewed
            && this.balanceSetManually == other.balanceSetManually;

    if (!rtn) {
      return false;
    }

    if (this.savings.size() != other.savings.size()) {
      return false;
    }

    if (this.corrections.size() != other.corrections.size()) {
      return false;
    }

    if (this.transactions.size() != other.transactions.size()) {
      return false;
    }

    var i = 0;
    while (i < this.savings.size()) {
      rtn = this.savings.get(i).equals(other.savings.get(i));
      if (!rtn) {
        return false;
      }

      i++;
    }

    i = 0;
    while (i < this.corrections.size()) {
      rtn = this.corrections.get(i).equals(other.corrections.get(i));
      if (!rtn) {
        return false;
      }

      i++;
    }

    i = 0;
    while (i < this.transactions.size()) {
      rtn = this.transactions.get(i).equals(other.transactions.get(i));
      if (!rtn) {
        return false;
      }

      i++;
    }

    return true;
  }

  public static class PostConstruct extends StdConverter<DailyBalance, DailyBalance> {

    @Override
    public DailyBalance convert(DailyBalance dailyBalance) {
      addPairedCorrectionsToTransactions(dailyBalance);
      createDayAverageBinding(dailyBalance);
      createNotPairedDailySpentBinding(dailyBalance);
      createBalanceWithSavingBinding(dailyBalance);
      dailyBalance.dailySavings =
          Bindings.createIntegerBinding(dailyBalance::getTotalSavings, dailyBalance.savings);

      dailyBalance.corrections.forEach(c -> c.setParentDailyBalance(dailyBalance));
      dailyBalance.transactions.forEach(t -> t.setDate(dailyBalance.getDate()));

      dailyBalance.reviewed.addListener(
          (observable, oldValue, newValue) -> dailyBalance.updateBalance());

      return dailyBalance;
    }

    private void addPairedCorrectionsToTransactions(DailyBalance dailyBalance) {
      for (var transaction : dailyBalance.transactions) {
        transaction.addAllPairedCorrection(
            dailyBalance.corrections.stream()
                .filter(c -> c.getPairedTransactionId() == transaction.getId())
                .collect(Collectors.toList()));
      }
    }

    private void createNotPairedDailySpentBinding(DailyBalance dailyBalance) {
      var currencyFormat = NumberFormat.getCurrencyInstance();
      currencyFormat.setMaximumFractionDigits(0);

      dailyBalance.unpairedDailySpentBinding =
          Bindings.createStringBinding(
              () ->
                  dailyBalance.getPredicted()
                      ? currencyFormat.format(dailyBalance.dayAverageBinding.get())
                      : currencyFormat.format(dailyBalance.getUnpairedDailySpent()),
              dailyBalance.transactions,
              dailyBalance.corrections);
    }

    private void createBalanceWithSavingBinding(DailyBalance dailyBalance) {
      dailyBalance.balanceWithSaving =
          Bindings.createIntegerBinding(
              () ->
                  dailyBalance.getBalance()
                      + dailyBalance.savings.stream().mapToInt(Saving::getAmount).sum(),
              dailyBalance.balance,
              dailyBalance.savings);
    }

    private void createDayAverageBinding(DailyBalance dailyBalance) {
      dailyBalance.dayAverageBinding =
          Bindings.createIntegerBinding(
              () -> {
                var allDailyBalances = dailyBalance.dataManager.getAllDailyBalances();

                var weekAverages = new ArrayList<Integer>();
                var monthDelta = 0;
                while (weekAverages.size() < 6) {
                  int finalMonthDelta = ++monthDelta;
                  var actDate = dailyBalance.getDate().minusMonths(finalMonthDelta);
                  var weekDays =
                      allDailyBalances.stream()
                          .filter(
                              db ->
                                  db.getDate().isAfter(actDate.minusDays(4))
                                      && db.getDate().isBefore(actDate.plusDays(4)))
                          .collect(Collectors.toList());
                  if (weekDays.get(weekDays.size() - 1).getReviewed()) {
                    var weekAverage =
                        (int)
                            Math.round(
                                weekDays.stream()
                                    .mapToInt(DailyBalance::getUnpairedDailySpent)
                                    .average()
                                    .orElseThrow());
                    weekAverages.add(weekAverage);
                  }
                }

                return (int)
                    Math.round(weekAverages.stream().mapToInt(w -> w).average().orElseThrow());
              });
    }
  }
}
