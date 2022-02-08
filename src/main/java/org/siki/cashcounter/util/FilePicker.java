package org.siki.cashcounter.util;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
@Builder(builderMethodName = "hiddenBuilder")
public class FilePicker {
  static final FileChooser fileChooser = new FileChooser();
  String title;
  @Singular Map<String, String[]> extensionFilters;
  Window parent;

  public static FilePickerBuilder builder(Window parent) {
    return hiddenBuilder().parent(parent);
  }

  public Optional<File> showDialog() {
    return ofNullable(fileChooser.showOpenDialog(parent));
  }
}
