package org.siki.cashcounter.view;

import javafx.collections.ObservableList;
import org.siki.cashcounter.service.CategoryService;
import org.siki.cashcounter.service.DataForViewService;
import org.siki.cashcounter.view.dialog.CorrectionDialog;
import org.siki.cashcounter.view.model.ObservableAccountTransaction;
import org.siki.cashcounter.view.model.ObservableCorrection;
import org.siki.cashcounter.view.model.ObservableDailyBalance;
import org.siki.cashcounter.view.model.ObservableMonthlyBalance;
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
      ObservableList<ObservableAccountTransaction> observableTransactions,
      DailyBalanceControl parent) {
    return new TransactionControl(observableTransactions, parent, dataForViewService);
  }

  public CorrectionDialog createNewCorrectionDialog(DailyBalanceControl parentDailyBalanceControl) {
    return new CorrectionDialog(dataForViewService, parentDailyBalanceControl);
  }

  public CorrectionDialog editCorrectionDialog(
      ObservableCorrection observableCorrection, DailyBalanceControl parentDailyBalanceControl) {
    return new CorrectionDialog(
        dataForViewService, observableCorrection, parentDailyBalanceControl);
  }

  public DailyBalanceControl createDailyBalanceControl(
      ObservableDailyBalance observableDailyBalance, MonthlyBalanceTitledPane parent) {
    return new DailyBalanceControl(observableDailyBalance, parent, this);
  }

  public CorrectionControl createCorrectionControl(
      ObservableCorrection observableCorrection, DailyBalanceControl parent) {
    return new CorrectionControl(observableCorrection, parent, categoryService, this);
  }
}
