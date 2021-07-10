package org.siki.cashcounter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Data
public class MonthlyBalance {
  private YearMonth yearMonth;
  private List<DailyBalance> dailyBalances;
  private List<Correction> predictions;

  public MonthlyBalance() {
    this.dailyBalances = new ArrayList<>();
    this.predictions = new ArrayList<>();
  }

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
  public boolean isValid() {
    return true;
  }
}
