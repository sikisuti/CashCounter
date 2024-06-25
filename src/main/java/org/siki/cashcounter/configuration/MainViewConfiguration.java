package org.siki.cashcounter.configuration;

import java.util.LinkedHashMap;
import javafx.scene.Node;
import javafx.stage.Window;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.service.DataForViewService;
import org.siki.cashcounter.service.PredictionService;
import org.siki.cashcounter.service.TaskExecutorService;
import org.siki.cashcounter.task.TaskFactory;
import org.siki.cashcounter.view.BusyVeil;
import org.siki.cashcounter.view.MainMenuBar;
import org.siki.cashcounter.view.MainScene;
import org.siki.cashcounter.view.MainTabPaneContent;
import org.siki.cashcounter.view.cashflow.CashFlowView;
import org.siki.cashcounter.view.dailycorrections.DailyCorrectionsView;
import org.siki.cashcounter.view.dialog.AlertFactory;
import org.siki.cashcounter.view.dialog.CategoriesDialog;
import org.siki.cashcounter.view.dialog.FileChooserFactory;
import org.siki.cashcounter.view.statistics.StatisticsView;
import org.siki.cashcounter.view.statisticstable.StatisticsTableView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MainViewConfiguration {
  @Bean
  public MainScene mainScene(
      MainMenuBar mainMenu, MainTabPaneContent mainContent, BusyVeil busyVeil) {
    return new MainScene(mainMenu, mainContent, busyVeil);
  }

  @Bean
  public Window mainWindow(MainScene mainScene) {
    return mainScene.getWindow();
  }

  @Bean
  public MainMenuBar mainMenu(
      PredictionService predictionService,
      CategoriesDialog categoriesDialog,
      DataManager dataManager,
      DataForViewService dataForViewService,
      FileChooserFactory fileChooserFactory,
      AlertFactory alertFactory,
      TaskExecutorService taskExecutorService,
      TaskFactory taskFactory) {
    return new MainMenuBar(
        predictionService,
        categoriesDialog,
        dataManager,
        dataForViewService,
        fileChooserFactory,
        alertFactory,
        taskExecutorService,
        taskFactory);
  }

  @Bean
  public MainTabPaneContent mainContent(
      DailyCorrectionsView dailyCorrectionsView,
      CashFlowView cashFlowView,
      StatisticsView statisticsView,
      StatisticsTableView statisticsTableView) {
    var tabs = new LinkedHashMap<String, Node>();
    tabs.put("Korrekciók", dailyCorrectionsView);
    tabs.put("Flow chart", cashFlowView);
    tabs.put("Statisztikák", statisticsView);
    tabs.put("Statisztikák2", statisticsTableView);
    return new MainTabPaneContent(tabs);
  }

  @Bean
  public BusyVeil busyVeil(TaskExecutorService taskExecutorService) {
    return new BusyVeil(taskExecutorService);
  }
}
