package org.siki.cashcounter.util;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class FilePickerTest {

  @Test
  void testBuilder() {
    var filePicker =
        FilePicker.builder(mock(Stage.class))
            .title("abc")
            .extensionFilter("excel", new String[] {"*.xlsx"})
            .build();
    assertEquals("abc", filePicker.getTitle());
  }
}
