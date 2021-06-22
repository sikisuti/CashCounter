package org.siki.cashcounter.view;

import javafx.collections.ObservableList;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.model.DailyBalance;
import org.siki.cashcounter.model.MonthlyBalance;
import org.siki.cashcounter.service.CategoryService;
import org.siki.cashcounter.service.DataForViewService;
import org.siki.cashcounter.view.dialog.CorrectionDialog;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.YearMonth;

public class ViewFactory {
  @Autowired private final DataForViewService dataForViewService;
  @Autowired private final CategoryService categoryService;

  public ViewFactory(DataForViewService dataForViewService, CategoryService categoryService) {
    this.dataForViewService = dataForViewService;
    this.categoryService = categoryService;
  }

  public MonthlyBalanceTitledPane createMonthlyBalanceTitledPane(MonthlyBalance monthlyBalance) {
    var monthlyBalanceTitledPane = new MonthlyBalanceTitledPane(monthlyBalance, this);
    monthlyBalanceTitledPane.expandedProperty().set(false);
    if (YearMonth.now().equals(monthlyBalance.getYearMonth())) {
      monthlyBalanceTitledPane.expandedProperty().set(true);
    }

    return monthlyBalanceTitledPane;
  }

  public TransactionControl createTransactionControl(
      ObservableList<AccountTransaction> transactions, DailyBalanceControl parent) {
    return new TransactionControl(transactions, parent, dataForViewService);
  }

  public CorrectionDialog createNewCorrectionDialog(DailyBalanceControl parentDailyBalanceControl) {
    return new CorrectionDialog(dataForViewService, parentDailyBalanceControl);
  }

  public CorrectionDialog editCorrectionDialog(
      Correction correction, DailyBalanceControl parentDailyBalanceControl) {
    return new CorrectionDialog(dataForViewService, correction, parentDailyBalanceControl);
  }

  public DailyBalanceControl createDailyBalanceControl(
      DailyBalance dailyBalance, MonthlyBalanceTitledPane parent) {
    return new DailyBalanceControl(dailyBalance, parent, this);
  }

  public CorrectionControl createCorrectionControl(
      Correction correction, DailyBalanceControl parent) {
    return new CorrectionControl(correction, parent, categoryService, this);
  }
}
