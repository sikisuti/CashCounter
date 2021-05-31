package org.siki.cashcounter.view;

import javafx.collections.ObservableList;
import org.siki.cashcounter.service.DataForViewService;
import org.siki.cashcounter.view.model.ObservableAccountTransaction;
import org.siki.cashcounter.view.model.ObservableMonthlyBalance;
import org.springframework.beans.factory.annotation.Autowired;

public class ControlFactory {
  @Autowired private final DataForViewService dataForViewService;

  public ControlFactory(DataForViewService dataForViewService) {
    this.dataForViewService = dataForViewService;
  }

  public MonthlyBalanceTitledPane createMonthlyBalanceTitledPane(
      ObservableMonthlyBalance observableMonthlyBalance) {
    return new MonthlyBalanceTitledPane(observableMonthlyBalance, this);
  }

  public TransactionControl createTransactionControl(
      ObservableList<ObservableAccountTransaction> observableTransactions,
      DailyBalanceControl parent) {
    return new TransactionControl(observableTransactions, parent, dataForViewService);
  }
}
