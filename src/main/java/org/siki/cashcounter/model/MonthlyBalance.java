package org.siki.cashcounter.model;

import lombok.Data;
import lombok.Getter;

import java.time.YearMonth;
import java.util.List;

@Data
public class MonthlyBalance {
    private YearMonth yearMonth;
    private List<DailyBalance> dailyBalances;
}
