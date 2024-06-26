package org.siki.cashcounter;

import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.service.TaskExecutorService;
import org.siki.cashcounter.task.TaskFactory;
import org.siki.cashcounter.view.MainScene;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@Slf4j
public class CashCounter extends Application {
  public static void main(String[] args) {
    launch();
  }

  @Override
  public void start(Stage stage) {
    var context = new AnnotationConfigApplicationContext("org.siki.cashcounter.configuration");
    var mainScene = context.getBean(MainScene.class);
    stage.setMaximized(true);
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
    loadData(context);
  }

  private void loadData(AnnotationConfigApplicationContext context) {
    var taskExecutorService = context.getBean(TaskExecutorService.class);
    var taskFactory = context.getBean(TaskFactory.class);
    var dataManager = context.getBean(DataManager.class);
    taskExecutorService.runTask(taskFactory.loadDataTask(), dataManager::loadData);
  }
}
