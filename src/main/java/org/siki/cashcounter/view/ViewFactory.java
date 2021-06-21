package org.siki.cashcounter.view;

import javafx.collections.ObservableList;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.model.DailyBalance;
import org.siki.cashcounter.service.CategoryService;
import org.siki.cashcounter.service.DataForViewService;
import org.siki.cashcounter.view.dialog.CorrectionDialog;
import org.siki.cashcounter.view.model.ObservableMonthlyBalance;
import org.siki.cashcounter.view.model.ObservableTransaction;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.YearMonth;

public class ViewFactory {
  @Autowired private final DataForViewService dataForViewService;
  @Autowired private final CategoryService categoryService;

  public ViewFactory(DataForViewService dataForViewService, CategoryService categoryService) {
    this.dataForViewService = dataForViewService;
    this.categoryService = categoryService;
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
      ObservableList<ObservableTransaction> observableTransactions, DailyBalanceControl parent) {
    return new TransactionControl(observableTransactions, parent, dataForViewService);
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
    return DailyBalanceControl.of(dailyBalance, parent, this);
  }

  public CorrectionControl createCorrectionControl(
      Correction correction, DailyBalanceControl parent) {
    return CorrectionControl.of(correction, parent, categoryService, this);
  }
}
