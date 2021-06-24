package org.siki.cashcounter.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.CategoryMatchingRule;
import org.siki.cashcounter.repository.DataManager;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
  @Mock private DataManager dataManager;

  @Test
  void testSetCategory() {
    AccountTransaction accountTransaction = new AccountTransaction();
    accountTransaction.setComment("E.ON Áramszolgáltató Kft.");

    when(dataManager.getCategoryMatchingRules())
        .thenReturn(
            Collections.singletonList(
                CategoryMatchingRule.builder().pattern("E.ON").category("Villany").build()));
    new CategoryService(dataManager).setCategory(accountTransaction);
    assertEquals("Villany", accountTransaction.getCategory());
  }
}
