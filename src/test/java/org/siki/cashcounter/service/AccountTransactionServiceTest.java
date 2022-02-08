package org.siki.cashcounter.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.siki.cashcounter.repository.DataManager;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(MockitoExtension.class)
class AccountTransactionServiceTest {
  @Mock CategoryService categoryService;
  @Mock DataManager dataManager;

  private AccountTransactionService service;

  @BeforeEach
  void init() {
    service = new AccountTransactionService(categoryService, dataManager);
  }

  @Test
  void importTransactionsFrom_xlsx() {
    var file = new File("src/test/resources/transactions.xlsx");

    var transactions = service.importTransactionsFrom(file);
    log.info("{} lines read from the xlsx file", transactions.size());
    assertThat(transactions).isNotEmpty();
  }
}
