package org.siki.cashcounter.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.repository.DataManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.text.Collator;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DataForViewService {

  @Autowired private final DataManager dataManager;

  private SortedList<String> categories;

  public void save() throws IOException {
    dataManager.save();
  }

  public ObservableList<String> getAllCategories() {
    if (categories == null || categories.isEmpty()) {
      categories = collectCategories();
    }

    return categories;
  }

  private SortedList<String> collectCategories() {
    return dataManager.getMonthlyBalances().stream()
        .flatMap(
            mb ->
                mb.getDailyBalances().stream()
                    .flatMap(
                        db -> db.getTransactions().stream().map(AccountTransaction::getCategory)))
        .distinct()
        .filter(c -> Optional.ofNullable(c).isPresent())
        .collect(Collectors.toCollection(FXCollections::observableArrayList))
        .sorted((o1, o2) -> Collator.getInstance(new Locale("hu", "HU")).compare(o1, o2));
  }
}
