package org.siki.cashcounter.configuration;

import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.service.AccountTransactionService;
import org.siki.cashcounter.service.CategoryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransactionConfiguration {
  @Bean
  public AccountTransactionService getTransactionService(
      DataManager dataManager, CategoryService categoryService) {
    return new AccountTransactionService(categoryService, dataManager);
  }
}
