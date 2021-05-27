package org.siki.cashcounter.repository;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.siki.cashcounter.model.DailyBalance;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;

public class DailyBalanceRepository {

  @Autowired private DataHolder dataHolder;

  public ObservableList<DailyBalance> getAllDailyBalances() {
    return FXCollections.observableList(
        dataHolder.getDataSource().getMonthlyBalances().stream()
            .flatMap(mb -> mb.getDailyBalances().stream())
            .collect(Collectors.toList()));
  }
}
