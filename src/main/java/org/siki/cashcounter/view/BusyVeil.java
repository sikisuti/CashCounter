package org.siki.cashcounter.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.siki.cashcounter.service.TaskExecutorService;

@SuppressWarnings("java:S110")
public class BusyVeil extends StackPane {
  private final TaskExecutorService taskExecutorService;

  public BusyVeil(TaskExecutorService taskExecutorService) {
    this.taskExecutorService = taskExecutorService;

    this.getChildren().addAll(veil(), progressInfo());
    StackPane.setAlignment(this, Pos.CENTER);
    this.visibleProperty().bind(taskExecutorService.isBusy);
  }

  private Region veil() {
    var veil = new Region();
    veil.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4)");
    return veil;
  }

  private VBox progressInfo() {
    var progressInfo = new VBox(progressIndicator(), progressMessage());
    progressInfo.setAlignment(Pos.CENTER);
    StackPane.setAlignment(progressInfo, Pos.CENTER);
    return progressInfo;
  }

  private ProgressIndicator progressIndicator() {
    var progressIndicator = new ProgressIndicator();
    progressIndicator.setStyle("-fx-min-width: 100; -fx-min-height: 100;");
    VBox.setVgrow(progressIndicator, Priority.ALWAYS);
    progressIndicator.progressProperty().bind(taskExecutorService.progressIndicator);
    return progressIndicator;
  }

  private Label progressMessage() {
    var progressMessage = new Label();
    progressMessage.setFont(new Font("System Bold", 18));
    progressMessage.textProperty().bind(taskExecutorService.progressMessage);
    return progressMessage;
  }
}
