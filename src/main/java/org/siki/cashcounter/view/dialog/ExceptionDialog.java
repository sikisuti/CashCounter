package org.siki.cashcounter.view.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import lombok.NoArgsConstructor;

import java.io.PrintWriter;
import java.io.StringWriter;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class ExceptionDialog {
  public static Alert get(Exception ex) {
    var alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Exception Dialog");
    alert.setHeaderText("An exception occured");
    alert.setContentText(ex.getMessage());

    // Create expandable Exception.
    var sw = new StringWriter();
    var pw = new PrintWriter(sw);
    ex.printStackTrace(pw);
    var exceptionText = sw.toString();

    var label = new Label("The exception stacktrace was:");

    var textArea = new TextArea(exceptionText);
    textArea.setEditable(false);
    textArea.setWrapText(true);

    textArea.setMaxWidth(Double.MAX_VALUE);
    textArea.setMaxHeight(Double.MAX_VALUE);
    GridPane.setVgrow(textArea, Priority.ALWAYS);
    GridPane.setHgrow(textArea, Priority.ALWAYS);

    var expContent = new GridPane();
    expContent.setMaxWidth(Double.MAX_VALUE);
    expContent.add(label, 0, 0);
    expContent.add(textArea, 0, 1);

    // Set expandable Exception into the dialog pane.
    alert.getDialogPane().setExpandableContent(expContent);

    return alert;
  }
}
