package org.siki.cashcounter.service;

import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.view.model.ObservableDailyBalance;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

@RequiredArgsConstructor
public class DailyBalanceService {
  @Autowired private final DataForViewService dataForViewService;

  public ObservableDailyBalance findDailyBalanceByDate(LocalDate date) {
    return dataForViewService.getObservableMonthlyBalances().stream()
        .flatMap(mb -> mb.getObservableDailyBalances().stream())
        .filter(d -> d.dateProperty().get().isEqual(date))
        .findFirst()
        .orElseThrow();
  }
}
