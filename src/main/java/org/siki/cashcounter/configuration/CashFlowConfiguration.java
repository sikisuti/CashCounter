package org.siki.cashcounter.configuration;

import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.service.ChartService;
import org.siki.cashcounter.view.cashflow.CashFlowChart;
import org.siki.cashcounter.view.cashflow.CashFlowView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CashFlowConfiguration {

  @Bean
  public CashFlowView cashFlowView(CashFlowChart cashFlowChart) {
    return new CashFlowView(cashFlowChart);
  }

  @Bean
  public CashFlowChart getCashFlowChart(ChartService chartService) {
    return new CashFlowChart(chartService);
  }

  @Bean
  public ChartService getChartService(DataManager dataManager) {
    return new ChartService(dataManager);
  }
}
