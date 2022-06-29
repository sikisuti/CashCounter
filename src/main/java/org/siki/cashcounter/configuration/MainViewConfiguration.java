package org.siki.cashcounter.configuration;

import javafx.scene.Node;
import javafx.stage.Window;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.service.AccountTransactionService;
import org.siki.cashcounter.service.DataForViewService;
import org.siki.cashcounter.service.PredictionService;
import org.siki.cashcounter.view.MainMenuBar;
import org.siki.cashcounter.view.MainScene;
import org.siki.cashcounter.view.MainTabPaneContent;
import org.siki.cashcounter.view.dialog.AlertFactory;
import org.siki.cashcounter.view.dialog.CategoriesDialog;
import org.siki.cashcounter.view.dialog.FileChooserFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;

@Configuration
public class MainViewConfiguration {
  @Bean
  public MainScene mainScene(Node mainMenu, Node mainContent) {
    return new MainScene(mainMenu, mainContent);
  }

  @Bean
  public Window mainWindow(MainScene mainScene) {
    return mainScene.getWindow();
  }

  @Bean
  public Node mainMenu(
      PredictionService predictionService,
      CategoriesDialog categoriesDialog,
      AccountTransactionService transactionService,
      DataManager dataManager,
      DataForViewService dataForViewService,
      FileChooserFactory fileChooserFactory,
      AlertFactory alertFactory) {
    return new MainMenuBar(
        predictionService,
        categoriesDialog,
        transactionService,
        dataManager,
        dataForViewService,
        fileChooserFactory,
        alertFactory);
  }

  @Bean
  public Node mainContent(
      Node dailyCorrectionsView, Node cashFlowView, Node statisticsView, Node statisticsTableView) {
    var tabs = new LinkedHashMap<String, Node>();
    tabs.put("Korrekciók", dailyCorrectionsView);
    tabs.put("Flow chart", cashFlowView);
    tabs.put("Statisztikák", statisticsView);
    tabs.put("Statisztikák2", statisticsTableView);
    return new MainTabPaneContent(tabs);
  }
}
