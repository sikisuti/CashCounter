package org.siki.cashcounter.repository;

import lombok.Data;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.model.DailyBalance;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Data
public class MonthlyBalanceRaw {
  private YearMonth yearMonth;
  private List<DailyBalance> dailyBalances = new ArrayList<>();
  private List<Correction> predictions = new ArrayList<>();
}
