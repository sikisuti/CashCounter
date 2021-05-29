package org.siki.cashcounter;

import javafx.application.Application;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.siki.cashcounter.view.MainScene;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@Slf4j
public class CashCounter extends Application {
  public static void main(String[] args) {
    launch();
  }

  ApplicationContext context;

  @Override
  public void start(Stage stage) throws Exception {
    context = new AnnotationConfigApplicationContext(Config.class);
    var mainScene = context.getBean(MainScene.class);
    stage.setScene(mainScene);
    stage.show();
  }
}
