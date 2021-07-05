package org.siki.cashcounter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class MonthlyBalance {
  private YearMonth yearMonth;
  private List<DailyBalance> dailyBalances;
  private Map<String, Integer> predictions;

  public MonthlyBalance() {
    this.dailyBalances = new ArrayList<>();
    this.predictions = new HashMap<>();
  }

  @JsonIgnore
  public void clearPredictions() {
    predictions.clear();
  }

  @JsonIgnore
  public void addPrediction(String category, int amount) {
    predictions.put(
        category, predictions.containsKey(category) ? predictions.get(category) + amount : amount);
  }

  @JsonIgnore
  public boolean isValid() {
    return true;
  }
}
