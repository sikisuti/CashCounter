package org.siki.cashcounter.view.model;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class ObservableMonthlyBalance {
  private ObjectProperty<YearMonth> yearMonth;
  private ObservableList<ObservableDailyBalance> dailyBalances;

  public String getYearMonthString() {
    return yearMonth.getValue().format(DateTimeFormatter.ofPattern("uuuu. MMMM"));
  }
}
