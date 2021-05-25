package org.siki.cashcounter;

import org.siki.cashcounter.repository.DataReporitoryImpl;
import org.siki.cashcounter.repository.DataRepository;
import org.siki.cashcounter.service.DataService;
import org.siki.cashcounter.service.DataServiceImpl;
import org.siki.cashcounter.view.MainScene;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
    @Bean
    public DataRepository getRepository() {
        return new DataReporitoryImpl();
    }

    @Bean
    public DataService getService() {
        return new DataServiceImpl();
    }

    @Bean
    public MainScene getMainScene() {
        return new MainScene();
    }
}
