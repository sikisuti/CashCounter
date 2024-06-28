package org.siki.cashcounter.view;

import javafx.collections.ObservableList;
import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.model.DailyBalance;
import org.siki.cashcounter.model.MonthlyBalance;
import org.siki.cashcounter.service.CategoryService;
import org.siki.cashcounter.service.CorrectionService;
import org.siki.cashcounter.service.DataForViewService;
import org.siki.cashcounter.view.dailycorrections.CorrectionControl;
import org.siki.cashcounter.view.dailycorrections.DailyBalanceControl;
import org.siki.cashcounter.view.dailycorrections.MonthlyBalanceTitledPane;
import org.siki.cashcounter.view.dailycorrections.TransactionListView;
import org.siki.cashcounter.view.dialog.CategoryChartDialog;
import org.siki.cashcounter.view.dialog.CorrectionDialog;
import org.siki.cashcounter.view.dialog.MonthlyPredictionsDialog;
import org.siki.cashcounter.view.dialog.monthlyinfo.MonthInfoDialog;

@RequiredArgsConstructor
public class ViewFactory {
  private final DataForViewService dataForViewService;
  private final CategoryService categoryService;
  private final CorrectionService correctionService;
  private final ConfigurationManager configurationManager;

  public TransactionListView createTransactionListView(
      ObservableList<AccountTransaction> transactions, DailyBalanceControl parent) {
    return new TransactionListView(transactions, parent, dataForViewService);
  }

  public CorrectionDialog createNewCorrectionDialog(DailyBalance parentDailyBalance) {
    return new CorrectionDialog(correctionService, parentDailyBalance);
  }

  public CorrectionDialog editCorrectionDialog(
      Correction correction, DailyBalance parentDailyBalance) {
    return new CorrectionDialog(correctionService, correction, parentDailyBalance);
  }

  public DailyBalanceControl createDailyBalanceControl(
      DailyBalance dailyBalance, MonthlyBalanceTitledPane parent) {
    return new DailyBalanceControl(dailyBalance, parent, this);
  }

  public CorrectionControl createCorrectionControl(
      Correction correction, DailyBalanceControl parent) {
    return new CorrectionControl(correction, parent, categoryService, this);
  }

  public CategoryChartDialog createCategoryChartDialog() {
    return new CategoryChartDialog(categoryService, configurationManager, correctionService);
  }

  public MonthInfoDialog getMonthInfoDialog(MonthlyBalance monthlyBalance) {
    return new MonthInfoDialog(monthlyBalance);
  }

  public MonthlyPredictionsDialog getMonthlyPredictionsDialog(MonthlyBalance monthlyBalance) {
    return new MonthlyPredictionsDialog(monthlyBalance);
  }
}
