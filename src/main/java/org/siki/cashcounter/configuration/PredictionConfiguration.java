package org.siki.cashcounter.configuration;

import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.service.CorrectionService;
import org.siki.cashcounter.service.DailyBalanceService;
import org.siki.cashcounter.service.PredictionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PredictionConfiguration {
  @Bean
  public PredictionService getPredictionService(
      DataManager dataManager,
      DailyBalanceService dailyBalanceService,
      CorrectionService correctionService) {
    return new PredictionService(dataManager, dailyBalanceService, correctionService);
  }
}
