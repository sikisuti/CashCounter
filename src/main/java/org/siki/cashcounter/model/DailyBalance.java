package org.siki.cashcounter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public final class DailyBalance {
  private DailyBalance prevDailyBalance;
  private LocalDate date;
  private int balance;
  private boolean balanceSetManually;
  private boolean predicted;
  private boolean reviewed;
  private int dailySpend;

  private List<Saving> savings;
  private List<Correction> corrections;
  private List<AccountTransaction> transactions;

  public void setPrevDailyBalance(DailyBalance prevDailyBalance) {
    this.prevDailyBalance = prevDailyBalance;
    if (prevDailyBalance != null) {
      setDailySpend(getBalance() - prevDailyBalance.getBalance() - getTotalCorrections());
    } else {
      setDailySpend(0);
    }
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
    if (prevDailyBalance != null) {
      setDailySpend(getBalance() - prevDailyBalance.getBalance() - getTotalCorrections());
    } else {
      setDailySpend(0);
    }
  }

  public void removeCorrection(Correction correction) {
    corrections.remove(correction);
    if (prevDailyBalance != null) {
      setDailySpend(getBalance() - prevDailyBalance.getBalance() - getTotalCorrections());
    } else {
      setDailySpend(0);
    }
  }

  public void addTransaction(AccountTransaction transaction) {
    transaction.setDailyBalance(this);
    transactions.add(transaction);
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

  public void calculateBalance() {
    int diff = transactions.stream().mapToInt(AccountTransaction::getAmount).sum();
    setBalance(prevDailyBalance.getBalance() + diff);
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
