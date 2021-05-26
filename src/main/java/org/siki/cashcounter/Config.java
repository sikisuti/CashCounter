package org.siki.cashcounter;

import org.siki.cashcounter.repository.DataHolder;
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
    public DataHolder getDataHolder() {
        return new DataHolder();
    }

    @Bean
    public DataManager getDataManager() {
        return new DataManager();
    }

    @Bean
    public ConfigurationManager configurationManager() throws IOException {
        return new ConfigurationManager();
    }
}
