package org.siki.cashcounter.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.AllArgsConstructor;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.service.converter.MonthlyBalanceMapper;
import org.siki.cashcounter.view.model.ObservableMonthlyBalance;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;

@AllArgsConstructor
public class DataForViewService {

  @Autowired private DataManager dataManager;
  @Autowired private MonthlyBalanceMapper monthlyBalanceMapper;

  public ObservableList<ObservableMonthlyBalance> getObservableMonthlyBalances() {
    return dataManager.getMonthlyBalances().stream()
        .map(mb -> monthlyBalanceMapper.toView(mb))
        .collect(Collectors.toCollection(FXCollections::observableArrayList));
  }
}
