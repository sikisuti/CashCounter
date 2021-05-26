package org.siki.cashcounter.model;

import javafx.collections.ObservableList;
import lombok.Getter;

@Getter
public class MonthlyBalance {
    private ObservableList<DailyBalance> dailyBalances;
}
