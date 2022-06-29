package org.siki.cashcounter.configuration;

import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.view.dialog.AlertFactory;
import org.siki.cashcounter.view.dialog.CategoriesDialog;
import org.siki.cashcounter.view.dialog.FileChooserFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DialogConfiguration {

  @Bean
  public CategoriesDialog categoriesDialog(DataManager dataManager) {
    return new CategoriesDialog(dataManager);
  }

  @Bean
  public FileChooserFactory fileChooserFactory() {
    return new FileChooserFactory();
  }

  @Bean
  public AlertFactory alertFactory() {
    return new AlertFactory();
  }
}
