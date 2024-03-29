package org.siki.cashcounter.configuration;

import javafx.scene.Node;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.view.ViewFactory;
import org.siki.cashcounter.view.statistics.StatisticsProvider;
import org.siki.cashcounter.view.statistics.StatisticsView;
import org.siki.cashcounter.view.statisticstable.StatisticsTableProvider;
import org.siki.cashcounter.view.statisticstable.StatisticsTableView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StatisticsConfiguration {

  @Bean
  public Node statisticsView(
      StatisticsProvider statisticsProvider, ConfigurationManager configurationManager) {
    return new StatisticsView(configurationManager, statisticsProvider);
  }

  @Bean
  public StatisticsProvider statisticsProvider(
      DataManager dataManager, ConfigurationManager configurationManager) {
    return new StatisticsProvider(dataManager, configurationManager);
  }

  @Bean
  public Node statisticsTableView(
      StatisticsTableProvider statisticsTableProvider, ViewFactory viewFactory) {
    return new StatisticsTableView(viewFactory, statisticsTableProvider);
  }

  @Bean
  public StatisticsTableProvider statisticsTableProvider(
      DataManager dataManager, ConfigurationManager configurationManager) {
    return new StatisticsTableProvider(dataManager, configurationManager);
  }
}
