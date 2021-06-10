package org.siki.cashcounter.view;

import javafx.collections.ObservableList;
import org.siki.cashcounter.service.DataForViewService;
import org.siki.cashcounter.view.dialog.CorrectionDialog;
import org.siki.cashcounter.view.model.ObservableAccountTransaction;
import org.siki.cashcounter.view.model.ObservableDailyBalance;
import org.siki.cashcounter.view.model.ObservableMonthlyBalance;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.YearMonth;

public class ViewFactory {
  @Autowired private final DataForViewService dataForViewService;

  public ViewFactory(DataForViewService dataForViewService) {
    this.dataForViewService = dataForViewService;
  }

  public MonthlyBalanceTitledPane createMonthlyBalanceTitledPane(
      ObservableMonthlyBalance observableMonthlyBalance) {
    var monthlyBalanceTitledPane = new MonthlyBalanceTitledPane(observableMonthlyBalance, this);
    boolean isThisMonth =
        YearMonth.now().equals(observableMonthlyBalance.getYearMonthProperty().get());
    monthlyBalanceTitledPane.expandedProperty().set(isThisMonth);
    if (isThisMonth) {
      monthlyBalanceTitledPane.fill();
    }

    return monthlyBalanceTitledPane;
  }

  public TransactionControl createTransactionControl(
      ObservableList<ObservableAccountTransaction> observableTransactions,
      DailyBalanceControl parent) {
    return new TransactionControl(observableTransactions, parent, dataForViewService);
  }

  public CorrectionDialog createNewCorrectionDialog() {
    return new CorrectionDialog(dataForViewService);
  }

  public DailyBalanceControl createDailyBalanceControl(
      ObservableDailyBalance observableDailyBalance, MonthlyBalanceTitledPane parent) {
    return new DailyBalanceControl(observableDailyBalance, parent, this);
  }
}
