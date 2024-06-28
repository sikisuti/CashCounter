package org.siki.cashcounter.service;

import static java.util.function.Function.identity;

import java.text.Collator;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.repository.DataManager;
import org.springframework.beans.factory.annotation.Autowired;

@RequiredArgsConstructor
public class CorrectionService {

  @Autowired private final DataManager dataManager;

  private SortedList<String> correctionTypes;
  private Long lastCorrectionId;

  public ObservableList<String> getAllCorrectionTypes() {
    if (correctionTypes == null || correctionTypes.isEmpty()) {
      correctionTypes = collectCorrectionTypes();
    }

    return correctionTypes;
  }

  private SortedList<String> collectCorrectionTypes() {
    return Stream.of(
            dataManager.getAllCorrections().stream()
                .filter(
                    c -> c.getParentDailyBalance().getDate().isAfter(LocalDate.now().minusYears(1)))
                .map(Correction::getType),
            dataManager.getAllTransactions().stream()
                .filter(t -> t.getDate().isAfter(LocalDate.now().minusYears(1)))
                .map(AccountTransaction::getCategory))
        .flatMap(identity())
        .filter(c -> Optional.ofNullable(c).isPresent())
        .distinct()
        .collect(Collectors.toCollection(FXCollections::observableArrayList))
        .sorted((o1, o2) -> Collator.getInstance(new Locale("hu", "HU")).compare(o1, o2));
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
