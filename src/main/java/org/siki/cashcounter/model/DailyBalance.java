package org.siki.cashcounter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.siki.cashcounter.model.converter.CorrectionListToObservableConverter;
import org.siki.cashcounter.model.converter.SavingListToObservableConverter;
import org.siki.cashcounter.model.converter.TransactionListToObservableConverter;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public final class DailyBalance {
  private ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
  private IntegerProperty balance = new SimpleIntegerProperty();
  private BooleanProperty balanceSetManually = new SimpleBooleanProperty();
  private BooleanProperty predicted = new SimpleBooleanProperty();
  private BooleanProperty reviewed = new SimpleBooleanProperty();
  private IntegerProperty dailySpent = new SimpleIntegerProperty();

  @JsonDeserialize(converter = SavingListToObservableConverter.class)
  private ObservableList<Saving> savings;

  @JsonDeserialize(converter = CorrectionListToObservableConverter.class)
  private ObservableList<Correction> corrections;

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

  public void setReviewed(boolean value) {
    reviewed.set(value);
  }

  public BooleanProperty reviewedProperty() {
    return reviewed;
  }

  public int getDailySpent() {
    return dailySpent.get();
  }

  public void setDailySpent(int value) {
    dailySpent.set(value);
  }

  public IntegerProperty dailySpentProperty() {
    return dailySpent;
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
    setDailySpent(dailySpent.get() - correction.getAmount());
    if (correction.isNotPaired()) {
      setBalance(balance.get() + correction.getAmount());
    }
  }

  public void removeCorrection(Correction correction) {
    corrections.remove(correction);
    setDailySpent(dailySpent.get() + correction.getAmount());
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

    if (this.getSavings().size() != other.getSavings().size()) {
      return false;
    }

    if (this.getCorrections().size() != other.getCorrections().size()) {
      return false;
    }

    if (this.getTransactions().size() != other.getTransactions().size()) {
      return false;
    }

    var i = 0;
    while (i < this.getSavings().size()) {
      rtn = this.getSavings().get(i).equals(other.getSavings().get(i));
      if (!rtn) {
        return false;
      }

      i++;
    }

    i = 0;
    while (i < this.getCorrections().size()) {
      rtn = this.getCorrections().get(i).equals(other.getCorrections().get(i));
      if (!rtn) {
        return false;
      }

      i++;
    }

    i = 0;
    while (i < this.getTransactions().size()) {
      rtn = this.getTransactions().get(i).equals(other.getTransactions().get(i));
      if (!rtn) {
        return false;
      }

      i++;
    }

    return true;
  }
}
