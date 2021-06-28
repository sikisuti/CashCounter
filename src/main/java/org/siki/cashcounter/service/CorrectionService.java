package org.siki.cashcounter.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.repository.DataManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CorrectionService {

  @Autowired private final DataManager dataManager;

  private ObservableList<String> correctionTypes;
  private Long lastCorrectionId;

  public ObservableList<String> getAllCorrectionTypes() {
    if (correctionTypes == null) {
      correctionTypes = FXCollections.observableArrayList();
    }

    if (correctionTypes.isEmpty()) {
      collectCorrectionTypes();
    }

    return correctionTypes;
  }

  private void collectCorrectionTypes() {
    correctionTypes.addAll(
        dataManager.getMonthlyBalances().stream()
            .flatMap(
                mb ->
                    mb.getDailyBalances().stream()
                        .flatMap(db -> db.getCorrections().stream().map(Correction::getType)))
            .distinct()
            .collect(Collectors.toList()));
  }

  public long getNextCorrectionId() {
    if (lastCorrectionId == null) {
      lastCorrectionId =
          dataManager.getMonthlyBalances().stream()
              .flatMap(
                  mb -> mb.getDailyBalances().stream().flatMap(db -> db.getCorrections().stream()))
              .mapToLong(Correction::getId)
              .max()
              .orElse(0);
    }

    return ++lastCorrectionId;
  }
}
