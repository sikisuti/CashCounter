package org.siki.cashcounter.service;

import javafx.beans.property.*;
import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.siki.cashcounter.view.dialog.AlertFactory;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class TaskExecutorService {
  private final AlertFactory alertFactory;
  public final BooleanProperty isBusy = new SimpleBooleanProperty();
  public final DoubleProperty progressIndicator = new SimpleDoubleProperty();
  public final StringProperty progressMessage = new SimpleStringProperty();

  public <T> void runTask(Task<T> task, Consumer<T> action) {
    isBusy.bind(task.runningProperty());
    progressIndicator.bind(task.progressProperty());
    progressMessage.bind(task.titleProperty());
    task.valueProperty()
        .addListener((observableValue, oldValue, newValue) -> action.accept(newValue));
    task.exceptionProperty()
        .addListener(
            (observableValue, oldThrowable, newThrowable) -> {
              log.error("Task execution error", newThrowable);
              alertFactory.forTaskExecutionError(newThrowable.getMessage());
            });
    new Thread(task).start();
  }
}
