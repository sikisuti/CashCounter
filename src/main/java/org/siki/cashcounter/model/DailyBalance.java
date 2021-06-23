package org.siki.cashcounter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.siki.cashcounter.model.converter.CorrectionListToObservableConverter;
import org.siki.cashcounter.model.converter.SavingListToObservableConverter;
import org.siki.cashcounter.model.converter.TransactionListToObservableConverter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(converter = DailyBalance.PostConstruct.class)
public final class DailyBalance {
  private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
  private final IntegerProperty balance = new SimpleIntegerProperty();
  private final BooleanProperty balanceSetManually = new SimpleBooleanProperty();
  private final BooleanProperty predicted = new SimpleBooleanProperty();
  private final BooleanProperty reviewed = new SimpleBooleanProperty();
  private final IntegerProperty uncoveredDailySpent = new SimpleIntegerProperty();

  private DailyBalance prevDailyBalance;

  @Getter
  @JsonDeserialize(converter = SavingListToObservableConverter.class)
  private ObservableList<Saving> savings;

  @Getter
  @JsonDeserialize(converter = CorrectionListToObservableConverter.class)
  private ObservableList<Correction> corrections;

  @Getter
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
  public int getUncoveredDailySpent() {
    return uncoveredDailySpent.get();
  }

  @JsonIgnore
  public void setUncoveredDailySpent(int value) {
    uncoveredDailySpent.set(value);
  }

  public IntegerProperty uncoveredDailySpentProperty() {
    return uncoveredDailySpent;
  }

  public void addSaving(Saving saving) {
    savings.add(saving);
  }

  @JsonIgnore
  public Integer getTotalSavings() {
    return savings.stream().mapToInt(Saving::getAmount).sum();
  }

  public void addCorrection(Correction correction) {
    corrections.add(correction);
    setUncoveredDailySpent(uncoveredDailySpent.get() - correction.getAmount());
    if (correction.isNotPaired()) {
      setBalance(balance.get() + correction.getAmount());
    }
  }

  public void removeCorrection(Correction correction) {
    corrections.remove(correction);
    setUncoveredDailySpent(uncoveredDailySpent.get() + correction.getAmount());
    if (correction.isNotPaired()) {
      setBalance(balance.get() - correction.getAmount());
    }
  }

  public void addTransaction(AccountTransaction transaction) {
    transactions.add(transaction);
    setBalance(balance.get() + transaction.getAmount());
  }

  public void addNonExistingTransactions(List<AccountTransaction> newTransactions) {
    findPossibleDuplicates(newTransactions);
    if (newTransactions.stream().filter(AccountTransaction::isPossibleDuplicate).count()
        == transactions.size()) {
      newTransactions.stream().filter(t -> !t.isPossibleDuplicate()).forEach(this::addTransaction);
    } else {
      newTransactions.forEach(this::addTransaction);
    }
  }

  public void calculateBalance(int previousBalance) {
    int diff = transactions.stream().mapToInt(AccountTransaction::getAmount).sum();
    setBalance(previousBalance + diff);
  }

  public int getAllDailySpent() {
    var transactionSum = transactions.stream().mapToInt(AccountTransaction::getAmount).sum();
    var notPairedCorrectionSum =
        corrections.stream().filter(Correction::isNotPaired).mapToInt(Correction::getAmount).sum();

    return transactionSum + notPairedCorrectionSum;
  }

  public void calculateUncoveredDailySpent() {}

  public Integer getTotalCorrections() {
    return corrections.stream().mapToInt(Correction::getAmount).sum();
  }

  public boolean isValid() {
    return transactions.stream().allMatch(AccountTransaction::isValid);
  }

  private void findPossibleDuplicates(List<AccountTransaction> newTransactions) {
    for (AccountTransaction newTransaction : newTransactions) {
      if (transactions.stream().anyMatch(t -> t.similar(newTransaction))) {
        newTransaction.setPossibleDuplicate(true);
      }
    }
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
      for (var transaction : dailyBalance.transactions) {
        transaction.addAllPairedCorrection(
            dailyBalance.corrections.stream()
                .filter(c -> c.getPairedTransactionId() == transaction.getId())
                .collect(Collectors.toList()));
      }
      return dailyBalance;
    }
  }
}
