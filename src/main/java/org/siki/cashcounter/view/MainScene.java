package org.siki.cashcounter.view;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainScene extends Scene {
  public MainScene(Node mainMenu, Node mainContent) {
    super(new StackPane(), 800, 600);

    var root = (StackPane) getRoot();
    var mainPane = new BorderPane();
    root.getChildren().addAll(mainPane);
    mainPane.setTop(mainMenu);
    mainPane.setCenter(mainContent);
  }

  // For future use
  private StackPane initBusyVeil() {
    var veil = new Region();
    veil.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4)");
    var progressIndicator = new ProgressIndicator();
    progressIndicator.setStyle("-fx-min-width: 100; -fx-min-height: 100;");
    VBox.setVgrow(progressIndicator, Priority.ALWAYS);
    var progressMessage = new Label();
    progressMessage.setFont(new Font("System Bold", 18));
    var progressInfo = new VBox(progressIndicator, progressMessage);
    progressInfo.setAlignment(Pos.CENTER);
    StackPane.setAlignment(progressInfo, Pos.CENTER);
    var busyVeil = new StackPane(veil, progressInfo);
    StackPane.setAlignment(busyVeil, Pos.CENTER);
    return busyVeil;
  }
}
