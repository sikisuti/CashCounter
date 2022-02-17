package org.siki.cashcounter.view;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class SelectableLabel extends Label {
  public SelectableLabel(String text) {
    StackPane textStack = new StackPane();
    javafx.scene.control.TextField textField = new javafx.scene.control.TextField(text);
    textField.setEditable(false);
    textField.setStyle(
        "-fx-background-color: transparent; -fx-background-insets: 0; -fx-background-radius: 0; -fx-padding: 0;");
    // the invisible label is a hack to get the textField to size like a label.
    javafx.scene.control.Label invisibleLabel = new javafx.scene.control.Label();
    invisibleLabel.textProperty().bind(textProperty());
    invisibleLabel.setVisible(false);
    textStack.getChildren().addAll(invisibleLabel, textField);
    textProperty().bindBidirectional(textField.textProperty());
    setGraphic(textStack);
    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
  }
}
