package org.siki.cashcounter.configuration;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import org.mapstruct.factory.Mappers;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.repository.converter.DataSourceMapper;
import org.siki.cashcounter.service.*;
import org.siki.cashcounter.task.TaskFactory;
import org.siki.cashcounter.view.ViewFactory;
import org.siki.cashcounter.view.dialog.AlertFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
  @Bean
  public DataManager getDataManager(ConfigurationManager configurationManager) {
    return new DataManager(configurationManager, Mappers.getMapper(DataSourceMapper.class));
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
      ConfigurationManager configurationManager) {
    return new ViewFactory(
        dataForViewService, categoryService, correctionService, configurationManager);
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
  public CategoryService getCategoryService(DataManager dataManager) {
    return new CategoryService(dataManager);
  }

  @Bean
  public TaskExecutorService taskRunnerService(AlertFactory alertFactory) {
    return new TaskExecutorService(alertFactory);
  }

  @Bean
  public SavingService savingService(
      ConfigurationManager configurationManager, ObjectMapper objectMapper) {
    return new SavingService(configurationManager, objectMapper);
  }

  @Bean
  public TaskFactory taskFactory(
      DataManager dataManager,
      ConfigurationManager configurationManager,
      ObjectMapper objectMapper,
      CategoryService categoryService,
      SavingService savingService) {
    return new TaskFactory(
        dataManager, configurationManager, objectMapper, categoryService, savingService);
  }

  @Bean
  public ObjectMapper objectMapper() {
    var objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    return objectMapper;
  }
}
