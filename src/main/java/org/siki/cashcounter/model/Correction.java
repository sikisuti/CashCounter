package org.siki.cashcounter.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public class Correction {

  private long id;
  private int amount;
  private String comment;
  private String type;
  //  private DailyBalance dailyBalance;
  //  private boolean paired;
  //  private AccountTransaction pairedTransaction;
  private long pairedTransactionId;

  public void setPairedTransaction(AccountTransaction transaction) {
    //    pairedTransaction = transaction;
    //    setPaired(transaction != null);
    setPairedTransactionId(transaction != null ? transaction.getId() : 0);
  }
}
