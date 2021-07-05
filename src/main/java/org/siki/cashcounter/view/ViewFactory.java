package org.siki.cashcounter.view;

import javafx.collections.ObservableList;
import javafx.scene.layout.GridPane;
import lombok.AllArgsConstructor;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.model.DailyBalance;
import org.siki.cashcounter.model.MonthlyBalance;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.service.CategoryService;
import org.siki.cashcounter.service.CorrectionService;
import org.siki.cashcounter.service.DataForViewService;
import org.siki.cashcounter.view.dialog.CorrectionDialog;
import org.siki.cashcounter.view.dialog.MonthInfoDialog;
import org.siki.cashcounter.view.statistics.StatisticsProvider;
import org.siki.cashcounter.view.statistics.StatisticsView;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.YearMonth;

@AllArgsConstructor
public class ViewFactory {
  @Autowired private final DataForViewService dataForViewService;
  @Autowired private final CategoryService categoryService;
  @Autowired private final CorrectionService correctionService;
  @Autowired private final DataManager dataManager;
  @Autowired private final ConfigurationManager configurationManager;

  public MonthlyBalanceTitledPane createMonthlyBalanceTitledPane(MonthlyBalance monthlyBalance) {
    var monthlyBalanceTitledPane = new MonthlyBalanceTitledPane(monthlyBalance, this);
    monthlyBalanceTitledPane.expandedProperty().set(false);
    if (YearMonth.now().equals(monthlyBalance.getYearMonth())) {
      monthlyBalanceTitledPane.expandedProperty().set(true);
    }

    return monthlyBalanceTitledPane;
  }

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

  public GridPane createStatisticsView() {
    var statisticsProvider = new StatisticsProvider(dataManager, configurationManager);
    return new StatisticsView(configurationManager, statisticsProvider);
  }

  public MonthInfoDialog getMonthInfoDialog(MonthlyBalance monthlyBalance) {
    return new MonthInfoDialog(monthlyBalance);
  }
}
