package org.siki.cashcounter.view.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.siki.cashcounter.model.MonthlyBalance;

import java.time.YearMonth;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

@Data
@NoArgsConstructor(access = PRIVATE)
public class ObservableMonthlyBalance {
  private ObjectProperty<YearMonth> yearMonthProperty;
  private ObservableList<ObservableDailyBalance> observableDailyBalances;

  private MonthlyBalance monthlyBalance;

  public static ObservableMonthlyBalance of(MonthlyBalance monthlyBalance) {
    var observableMonthlyBalance = new ObservableMonthlyBalance();
    observableMonthlyBalance.monthlyBalance = monthlyBalance;
    observableMonthlyBalance.yearMonthProperty =
        new SimpleObjectProperty<>(monthlyBalance.getYearMonth());
    observableMonthlyBalance.observableDailyBalances =
        monthlyBalance.getDailyBalances().stream()
            .map(ObservableDailyBalance::of)
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
    return observableMonthlyBalance;
  }

  public boolean isValid() {}
}
