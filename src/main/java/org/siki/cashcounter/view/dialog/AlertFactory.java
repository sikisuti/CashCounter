package org.siki.cashcounter.view.dialog;

import javafx.scene.control.Alert;

public class AlertFactory {
  public void forPredictionsLoaded() {
    var alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Üzenet");
    alert.setHeaderText("Végrehajtva");
    alert.setContentText("Korrekciók betöltve");
    alert.showAndWait();
  }

  public void forImportResult(int noOfImportedTransactions) {
    var alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Üzenet");
    alert.setHeaderText("Importálás kész");
    alert.setContentText(noOfImportedTransactions + " új tranzakció importálva");
    alert.showAndWait();
  }

  public void forTaskExecutionError(String message) {
    var alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Hiba");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
