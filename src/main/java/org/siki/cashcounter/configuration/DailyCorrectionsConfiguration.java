package org.siki.cashcounter.configuration;

import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.view.ViewFactory;
import org.siki.cashcounter.view.dailycorrections.DailyCorrectionsView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DailyCorrectionsConfiguration {

  @Bean
  public DailyCorrectionsView dailyCorrectionsView(
      DataManager dataManager, ViewFactory viewFactory) {
    return new DailyCorrectionsView(dataManager, viewFactory);
  }
}
