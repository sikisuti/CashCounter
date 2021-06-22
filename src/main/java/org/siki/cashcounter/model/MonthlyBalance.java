package org.siki.cashcounter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.YearMonth;
import java.util.List;

@Data
public class MonthlyBalance {
  private YearMonth yearMonth;
  private List<DailyBalance> dailyBalances;

  @JsonIgnore
  public boolean isValid() {
    return true;
  }
}
