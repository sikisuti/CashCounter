package org.siki.cashcounter.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.service.converter.MonthlyBalanceMapper;
import org.siki.cashcounter.view.model.ObservableMonthlyBalance;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DataForViewService {

  @Autowired private final DataManager dataManager;
  @Autowired private final MonthlyBalanceMapper monthlyBalanceMapper;

  private ObservableList<String> categories;
  private ObservableList<String> correctionTypes;

  public ObservableList<ObservableMonthlyBalance> getObservableMonthlyBalances() {
    return dataManager.getMonthlyBalances().stream()
        .map(monthlyBalanceMapper::toView)
        .collect(Collectors.toCollection(FXCollections::observableArrayList));
  }

  public ObservableList<String> getAllCorrectionTypes() {
    if (correctionTypes == null) {
      correctionTypes = FXCollections.observableArrayList();
    }

    if (correctionTypes.isEmpty()) {
      collectCorrectionTypes();
    }

    return correctionTypes;
  }

  public ObservableList<String> getAllCategories() {
    if (categories == null) {
      categories = FXCollections.observableArrayList();
    }

    if (categories.isEmpty()) {
      collectCategories();
    }

    return categories;
  }

  private void collectCorrectionTypes() {
    correctionTypes.addAll(
        dataManager.getMonthlyBalances().stream()
            .flatMap(
                mb ->
                    mb.getDailyBalances().stream()
                        .flatMap(db -> db.getCorrections().stream().map(Correction::getType)))
            .collect(Collectors.toList()));
  }

  private void collectCategories() {
    categories.addAll(
        dataManager.getMonthlyBalances().stream()
            .flatMap(
                mb ->
                    mb.getDailyBalances().stream()
                        .flatMap(
                            db ->
                                db.getTransactions().stream().map(AccountTransaction::getCategory)))
            .collect(Collectors.toList()));
  }
}
