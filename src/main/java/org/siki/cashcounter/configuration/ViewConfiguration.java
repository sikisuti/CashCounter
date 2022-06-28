package org.siki.cashcounter.configuration;

import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.service.AccountTransactionService;
import org.siki.cashcounter.service.DataForViewService;
import org.siki.cashcounter.service.PredictionService;
import org.siki.cashcounter.view.MainMenuBar;
import org.siki.cashcounter.view.MainScene;
import org.siki.cashcounter.view.ViewFactory;
import org.siki.cashcounter.view.chart.CashFlowChart;
import org.siki.cashcounter.view.dialog.CategoriesDialog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ViewConfiguration {
  @Bean
  public MainScene getMainScene(
      CashFlowChart cashFlowChart,
      ConfigurationManager configurationManager,
      ViewFactory viewFactory,
      DataForViewService dataForViewService,
      AccountTransactionService accountTransactionService,
      DataManager dataManager,
      PredictionService predictionService,
      MainMenuBar mainMenuBar) {
    return new MainScene(
        mainMenuBar,
        configurationManager,
        viewFactory,
        dataForViewService,
        accountTransactionService,
        dataManager,
        cashFlowChart,
        predictionService);
  }

  @Bean
  public MainMenuBar mainMenuBar(
      PredictionService predictionService,
      CategoriesDialog categoriesDialog,
      AccountTransactionService transactionService,
      DataManager dataManager,
      DataForViewService dataForViewService) {
    return new MainMenuBar(
        predictionService, categoriesDialog, transactionService, dataManager, dataForViewService);
  }

  @Bean
  public CategoriesDialog getCategoriesDialog(DataManager dataManager) {
    return new CategoriesDialog(dataManager);
  }
}
