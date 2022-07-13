package org.siki.cashcounter.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.service.CategoryService;
import org.siki.cashcounter.service.SavingService;

import java.io.File;

@RequiredArgsConstructor
public class TaskFactory {
  private final DataManager dataManager;
  private final ConfigurationManager configurationManager;
  private final ObjectMapper objectMapper;
  private final CategoryService categoryService;
  private final SavingService savingService;

  public LoadDataTask loadDataTask() {
    return new LoadDataTask(configurationManager, dataManager, objectMapper, savingService);
  }

  public ImportTransactionsTask importTransactionsTask(File transactionFile) {
    return new ImportTransactionsTask(transactionFile, categoryService, dataManager);
  }
}
