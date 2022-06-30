package org.siki.cashcounter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.util.StopWatch;
import org.siki.cashcounter.view.MainScene;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;

@Slf4j
public class CashCounter extends Application {
  public static void main(String[] args) {
    launch();
  }

  @Override
  public void start(Stage stage) {
    StopWatch.start("container");
    var context = new AnnotationConfigApplicationContext("org.siki.cashcounter.configuration");
    StopWatch.stop("container");
    StopWatch.start("view");
    var mainScene = context.getBean(MainScene.class);
    stage.setScene(mainScene);
    stage.show();
    stage.setOnCloseRequest(
        (WindowEvent event) -> {
          var dataManager = context.getBean(DataManager.class);
          try {
            dataManager.save();
          } catch (IOException ex) {
            log.error("Error while save on closing", ex);
          } finally {
            Platform.exit();
          }
        });
    StopWatch.stop("view");
    StopWatch.start("data");
    context.getBean(DataManager.class).loadData();
    StopWatch.stop("data");
  }
}
