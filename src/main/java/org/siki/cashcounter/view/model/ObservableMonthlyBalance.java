package org.siki.cashcounter.view.model;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;

import java.time.YearMonth;

public class ObservableMonthlyBalance {
  private ObjectProperty<YearMonth> yearMonth;
  private ObservableList<ObservableDailyBalance> dailyBalances;
}
