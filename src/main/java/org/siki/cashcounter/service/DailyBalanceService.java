package org.siki.cashcounter.service;

import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.model.DailyBalance;
import org.siki.cashcounter.model.MonthlyBalance;
import org.siki.cashcounter.repository.DataManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
public class DailyBalanceService {
  @Autowired private final DataManager dataManager;

  public List<MonthlyBalance> createMissingMonthlyBalancesUntil(YearMonth yearMonth) {
    var lastMonthlyBalance =
        dataManager.getMonthlyBalances().stream()
            .max(Comparator.comparing(MonthlyBalance::getYearMonth))
            .orElseThrow();
    var newMonthlyBalances = new ArrayList<MonthlyBalance>();
    while (lastMonthlyBalance.getYearMonth().isBefore(yearMonth)) {
      var createdMonthlyBalance = createMonthlyBalance(lastMonthlyBalance);
      newMonthlyBalances.add(createdMonthlyBalance);
      lastMonthlyBalance = createdMonthlyBalance;
    }

    return newMonthlyBalances;
  }

  public DailyBalance getLastDailyBalance() {
    return dataManager.getMonthlyBalances().stream()
        .flatMap(mb -> mb.getDailyBalances().stream())
        .reduce((first, second) -> second)
        .orElse(null);
  }

  private MonthlyBalance createMonthlyBalance(MonthlyBalance previousMonthlyBalance) {
    var newYearMonth = previousMonthlyBalance.getYearMonth().plusMonths(1);
    var newMonthlyBalance = new MonthlyBalance();
    newMonthlyBalance.setYearMonth(newYearMonth);
    List<DailyBalance> dailyBalances = new ArrayList<>();
    var lastDailyBalance =
        previousMonthlyBalance
            .getDailyBalances()
            .get(previousMonthlyBalance.getDailyBalances().size() - 1);
    while (lastDailyBalance.getDate().isBefore(newYearMonth.atEndOfMonth())) {
      var dailyBalance = createDailyBalance(lastDailyBalance);
      dailyBalances.add(dailyBalance);
      lastDailyBalance = dailyBalance;
    }

    newMonthlyBalance.setDailyBalances(dailyBalances);

    return newMonthlyBalance;
  }

  private DailyBalance createDailyBalance(DailyBalance previousDailyBalance) {
    var newDb = new DailyBalance();
    newDb.setPrevDailyBalance(previousDailyBalance);
    newDb.setDataManager(dataManager);
    newDb.setDate(previousDailyBalance.getDate().plusDays(1));
    //    newDb.updateBalance();
    //    previousDailyBalance.balanceProperty().addListener(observable -> newDb.updateBalance());
    newDb.setPredicted(Boolean.TRUE);
    newDb.setReviewed(Boolean.FALSE);

    new DailyBalance.PostConstruct().convert(newDb);

    return newDb;
  }
}
