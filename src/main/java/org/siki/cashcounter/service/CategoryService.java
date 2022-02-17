package org.siki.cashcounter.service;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.repository.DataManager;
import org.springframework.beans.factory.annotation.Autowired;

@RequiredArgsConstructor
public class CategoryService {

  @Autowired private final DataManager dataManager;

  private final StringProperty selectedCategory = new SimpleStringProperty();

  public String getSelectedCategory() {
    return selectedCategory.get();
  }

  public void setSelectedCategory(String selectedCategory) {
    this.selectedCategory.set(selectedCategory);
  }

  public StringProperty selectedCategoryProperty() {
    return selectedCategory;
  }

  public void setCategory(AccountTransaction accountTransaction) {
    dataManager.getCategoryMatchingRules().keySet().stream()
        .filter(
            c ->
                dataManager.getCategoryMatchingRules().get(c).stream()
                    .anyMatch(p -> dataManager.isCategoryMatch(accountTransaction, p)))
        .findFirst()
        .ifPresent(accountTransaction::setCategory);
  }
}
