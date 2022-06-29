package org.siki.cashcounter.view.dialog;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Optional;

public class FileChooserFactory {

  public Optional<File> forPredictions(Window window) {
    var fileChooser = new FileChooser();
    fileChooser.setTitle("Válaszd ki a korrekciós fájlt");
    fileChooser
        .getExtensionFilters()
        .addAll(
            new FileChooser.ExtensionFilter("json files", "*.jsn"),
            new FileChooser.ExtensionFilter("Minden fájl", "*.*"));
    return Optional.ofNullable(fileChooser.showOpenDialog(window));
  }

  public Optional<File> forTransactions(Window window) {
    var fileChooser = new FileChooser();
    fileChooser.setTitle("Válaszd ki a fájlt");
    fileChooser
        .getExtensionFilters()
        .addAll(
            new FileChooser.ExtensionFilter("excel", "*.xlsx"),
            new FileChooser.ExtensionFilter("csv files", "*.csv"),
            new FileChooser.ExtensionFilter("Minden fájl", "*.*"));
    return Optional.ofNullable(fileChooser.showOpenDialog(window));
  }
}
