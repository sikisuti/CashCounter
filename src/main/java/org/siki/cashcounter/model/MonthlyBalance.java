package org.siki.cashcounter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class MonthlyBalance {
  private YearMonth yearMonth;
  private List<DailyBalance> dailyBalances = new ArrayList<>();
  private List<Correction> predictions = new ArrayList<>();
  private MonthlyBalance previousMonthlyBalance;
  private MonthlyBalance nextMonthlyBalance;

  @JsonIgnore
  public void clearPredictions() {
    predictions.clear();
  }

  @JsonIgnore
  public void addPrediction(String category, String comment, int amount) {
    var predictedCorrection = new Correction();
    predictedCorrection.setType(category);
    predictedCorrection.setComment(comment);
    predictedCorrection.setAmount(amount);
    predictions.add(predictedCorrection);
  }

  @JsonIgnore
  public int addTransactions(List<AccountTransaction> transactions) {
    var dailyGroupedTransactions =
        transactions.stream().collect(Collectors.groupingBy(AccountTransaction::getDate));

    int counter = 0;
    for (var entry : dailyGroupedTransactions.entrySet()) {
      counter +=
          dailyBalances.stream()
              .filter(db -> db.getDate().isEqual(entry.getKey()))
              .findFirst()
              .orElseThrow()
              .addTransactions(entry.getValue());
    }

    return counter;
  }

  @JsonIgnore
  public boolean isValid() {
    return true;
  }
}
