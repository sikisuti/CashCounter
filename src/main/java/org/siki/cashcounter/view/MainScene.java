package org.siki.cashcounter.view;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainScene extends Scene {
  public MainScene(Node mainMenu, Node mainContent, BusyVeil busyVeil) {
    super(new StackPane(), 800, 600);

    var root = (StackPane) getRoot();
    var mainPane = new BorderPane();
    mainPane.setTop(mainMenu);
    mainPane.setCenter(mainContent);
    root.getChildren().addAll(mainPane, busyVeil);
  }
}
