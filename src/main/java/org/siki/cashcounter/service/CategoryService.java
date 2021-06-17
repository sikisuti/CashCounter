package org.siki.cashcounter.service;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.CategoryMatchingRule;
import org.siki.cashcounter.repository.DataManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

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
    Optional<CategoryMatchingRule> matchingRule =
        dataManager.getCategoryMatchingRules().stream()
            .filter(
                mr ->
                    accountTransaction
                            .getComment()
                            .toLowerCase()
                            .contains(mr.getPattern().toLowerCase())
                        || accountTransaction
                            .getType()
                            .toLowerCase()
                            .contains(mr.getPattern().toLowerCase())
                        || accountTransaction
                            .getOwner()
                            .toLowerCase()
                            .contains(mr.getPattern().toLowerCase()))
            .findFirst();
    String category = null;
    if (matchingRule.isPresent()) {
      category = matchingRule.get().getCategory();
    }

    accountTransaction.setCategory(category);
  }
}
