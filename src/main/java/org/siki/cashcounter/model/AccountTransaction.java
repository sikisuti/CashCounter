package org.siki.cashcounter.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountTransaction {

  private long id;
  private String transactionType;
  private LocalDate date;
  private int amount;
  private int balance;
  private String accountNumber;
  private String owner;
  private String comment;
  private String counter;
  private String category;
  private boolean paired;
  private List<Correction> pairedCorrections;
  private boolean possibleDuplicate;
  private DailyBalance dailyBalance;

  public void addPairedCorrection(Correction correction) {
    if (!pairedCorrections.contains(correction)) {
      pairedCorrections.add(correction);
    }
    setPaired(true);
  }

  public void removePairedCorrection(Correction correction) {
    pairedCorrections.remove(correction);
    if (pairedCorrections.isEmpty()) {
      setPaired(false);
    }
  }

  public Integer getNotPairedAmount() {
    return getAmount() - pairedCorrections.stream().mapToInt(Correction::getAmount).sum();
  }

  public boolean isValid() {
    return !isPossibleDuplicate() && getCategory() != null && !getCategory().isEmpty();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || this.getClass() != obj.getClass()) {
      return false;
    }

    AccountTransaction other = (AccountTransaction) obj;

    if (this.getCategory() == null) {
      if (other.getCategory() != null) {
        return false;
      }
    } else {
      if (!this.getCategory().equalsIgnoreCase(other.getCategory())) {
        return false;
      }
    }

    return this.getId() == other.getId()
        && this.getTransactionType().equals(other.getTransactionType())
        && this.getAmount() == other.getAmount()
        && this.getBalance() == other.getBalance()
        && this.getAccountNumber().equals(other.getAccountNumber())
        && this.getOwner().equals(other.getOwner())
        && this.getComment().equals(other.getComment())
        && this.getCounter().equals(other.getCounter());
  }

  public boolean similar(Object obj) {
    if (obj == null || this.getClass() != obj.getClass()) {
      return false;
    }

    AccountTransaction other = (AccountTransaction) obj;

    return this.getTransactionType().equals(other.getTransactionType())
        && this.getAmount() == other.getAmount();
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id,
        transactionType,
        date,
        amount,
        balance,
        accountNumber,
        owner,
        comment,
        counter,
        category,
        paired,
        pairedCorrections);
  }
}
