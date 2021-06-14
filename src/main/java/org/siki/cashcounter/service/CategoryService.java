package org.siki.cashcounter.service;

import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.CategoryMatchingRule;
import org.siki.cashcounter.repository.DataManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@RequiredArgsConstructor
public class CategoryService {

  @Autowired private final DataManager dataManager;

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
