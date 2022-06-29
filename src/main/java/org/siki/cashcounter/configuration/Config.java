package org.siki.cashcounter.configuration;

import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.service.CategoryService;
import org.siki.cashcounter.service.CorrectionService;
import org.siki.cashcounter.service.DailyBalanceService;
import org.siki.cashcounter.service.DataForViewService;
import org.siki.cashcounter.view.ViewFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class Config {
  @Bean
  public DataManager getDataManager(ConfigurationManager configurationManager) {
    return new DataManager(configurationManager);
  }

  @Bean
  public ConfigurationManager configurationManager() throws IOException {
    return new ConfigurationManager("./config.properties");
  }

  @Bean
  public DataForViewService getDataForViewService(DataManager dataManager) {
    return new DataForViewService(dataManager);
  }

  @Bean
  public ViewFactory getViewFactory(
      DataForViewService dataForViewService,
      CategoryService categoryService,
      CorrectionService correctionService,
      DataManager dataManager,
      ConfigurationManager configurationManager) {
    return new ViewFactory(
        dataForViewService, categoryService, correctionService, dataManager, configurationManager);
  }

  @Bean
  public CorrectionService getCorrectionService(DataManager dataManager) {
    return new CorrectionService(dataManager);
  }

  @Bean
  public DailyBalanceService getDailyBalanceService(DataManager dataManager) {
    return new DailyBalanceService(dataManager);
  }

  @Bean
  public CategoryService getCategoryService(
      DataManager dataManager, ConfigurationManager configurationManager) {
    return new CategoryService(dataManager, configurationManager);
  }
}
