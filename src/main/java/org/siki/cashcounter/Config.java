package org.siki.cashcounter;

import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.service.ChartService;
import org.siki.cashcounter.view.MainScene;
import org.siki.cashcounter.view.chart.CashFlowChart;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class Config {

  @Bean
  public ChartService getChartService(DataManager dataManager) {
    return new ChartService(dataManager);
  }

  @Bean
  public CashFlowChart getCashFlowChart(ChartService chartService) {
    return new CashFlowChart(chartService);
  }

  @Bean
  public MainScene getMainScene(
      CashFlowChart cashFlowChart, ConfigurationManager configurationManager) {
    return new MainScene(cashFlowChart, configurationManager);
  }

  @Bean
  public DataManager getDataManager(ConfigurationManager configurationManager) {
    return new DataManager(configurationManager);
  }

  @Bean
  public ConfigurationManager configurationManager() throws IOException {
    return new ConfigurationManager("./config.properties");
  }
}
