package org.siki.cashcounter.service;

import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.model.DailyBalance;
import org.siki.cashcounter.model.MonthlyBalance;
import org.siki.cashcounter.repository.DataManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.YearMonth;

@RequiredArgsConstructor
public class DailyBalanceService {
  @Autowired private final DataManager dataManager;

  public DailyBalance findDailyBalanceByDate(LocalDate date) {
    return dataManager.getMonthlyBalances().stream()
        .flatMap(mb -> mb.getDailyBalances().stream())
        .filter(d -> d.dateProperty().get().isEqual(date))
        .findFirst()
        .orElseThrow();
  }

  public DailyBalance getOrCreateDailyBalance(LocalDate date) {
    return dataManager.getMonthlyBalances().stream()
        .flatMap(mb -> mb.getDailyBalances().stream())
        .filter(d -> d.getDate().equals(date))
        .findFirst()
        .orElseGet(
            () -> {
              var lastDailyBalance = getLastDailyBalance();
              //          var newDb = new DailyBalance();
              DailyBalance newDb = null;
              // fill the possible date gaps
              while (!lastDailyBalance.getDate().equals(date)) {
                newDb = new DailyBalance();
                newDb.setPrevDailyBalance(lastDailyBalance);
                newDb.setDataManager(dataManager);
                newDb.setPrevDailyBalance(getLastDailyBalance());
                newDb.setDate(getLastDailyBalance().getDate().plusDays(1));
                newDb.updateBalance();
                DailyBalance finalNewDb = newDb;
                lastDailyBalance
                    .balanceProperty()
                    .addListener((observable, oldValue, newValue) -> finalNewDb.updateBalance());
                newDb.setPredicted(Boolean.TRUE);
                newDb.setReviewed(Boolean.FALSE);

                new DailyBalance.PostConstruct().convert(newDb);

                getOrCreateMonthlyBalance(newDb.getDate()).getDailyBalances().add(newDb);
                lastDailyBalance = newDb;
              }

              return newDb;
            });
  }

  public DailyBalance getLastDailyBalance() {
    return dataManager.getMonthlyBalances().stream()
        .flatMap(mb -> mb.getDailyBalances().stream())
        .reduce((first, second) -> second)
        .orElse(null);
  }

  private MonthlyBalance getOrCreateMonthlyBalance(LocalDate date) {
    return dataManager.getMonthlyBalances().stream()
        .filter(mb -> mb.getYearMonth().equals(YearMonth.from(date)))
        .findFirst()
        .orElseGet(
            () -> {
              var newMonthlyBalance = new MonthlyBalance();
              newMonthlyBalance.setYearMonth(YearMonth.from(date));
              dataManager.getMonthlyBalances().add(newMonthlyBalance);
              return newMonthlyBalance;
            });
  }
}
