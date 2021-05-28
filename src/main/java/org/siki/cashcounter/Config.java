package org.siki.cashcounter;

import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.view.MainScene;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class Config {
  /*@Bean
  public DataRepository getRepository() {
      return new DataReporitoryImpl();
  }

  @Bean
  public DataService getService() {
      return new DataServiceImpl();
  }*/

  @Bean
  public MainScene getMainScene() {
    return new MainScene();
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
