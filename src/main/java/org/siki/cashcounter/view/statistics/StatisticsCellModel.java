package org.siki.cashcounter.view.statistics;

import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.Correction;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StatisticsCellModel {
  private final Set<Correction> corrections = new HashSet<>();
  private final Set<AccountTransaction> transactions = new HashSet<>();
  private Integer average;
  private StatisticsCellModel previousStatisticsModel;

  public void putCorrection(Correction correction) {
    corrections.add(correction);
  }

  public void putAllCorrections(List<Correction> corrections) {
    this.corrections.addAll(corrections);
  }

  public Set<Correction> getCorrections() {
    return corrections;
  }

  public void putTransaction(AccountTransaction transaction) {
    transactions.add(transaction);
  }

  public void putAllTransactions(List<AccountTransaction> transactions) {
    this.transactions.addAll(transactions);
  }

  public Set<AccountTransaction> getTransactions() {
    return transactions;
  }

  public Integer getAmount() {
    var amount = 0;
    if (!corrections.isEmpty()) {
      amount = corrections.stream().mapToInt(Correction::getAmount).sum();
    } else if (!transactions.isEmpty()) {
      amount = transactions.stream().mapToInt(AccountTransaction::getUnpairedAmount).sum();
    }

    return amount;
  }

  public String getDetails() {
    var details = "";
    if (!corrections.isEmpty()) {
      details =
          corrections.stream()
              .map(
                  c ->
                      String.format(
                          "%15s  %s",
                          new DecimalFormat("#,###,###' Ft'").format(c.getAmount()),
                          c.getComment()))
              .collect(Collectors.joining("\n"));
    } else if (!transactions.isEmpty()) {
      details =
          transactions.stream()
              .map(
                  t ->
                      t.getType()
                          + "\t"
                          + t.getComment()
                          + "\t"
                          + NumberFormat.getCurrencyInstance().format(t.getAmount()))
              .collect(Collectors.joining("\n"));
    }

    return details;
  }

  public void setAverage(Integer average) {
    this.average = average;
  }

  public Integer getAverage() {
    return average;
  }

  public void setPreviousStatisticsModel(StatisticsCellModel statisticsModel) {
    this.previousStatisticsModel = statisticsModel;
  }

  public StatisticsCellModel getPreviousStatisticsModel() {
    return previousStatisticsModel;
  }
}
