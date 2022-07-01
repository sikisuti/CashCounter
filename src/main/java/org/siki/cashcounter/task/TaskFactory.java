package org.siki.cashcounter.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.service.CategoryService;

import java.io.File;

@RequiredArgsConstructor
public class TaskFactory {
  private final DataManager dataManager;
  private final ConfigurationManager configurationManager;
  private final ObjectMapper objectMapper;
  private final CategoryService categoryService;

  public LoadDataTask loadDataTask() {
    return new LoadDataTask(configurationManager, dataManager, objectMapper);
  }

  public ImportTransactionsTask importTransactionsTask(File transactionFile) {
    return new ImportTransactionsTask(transactionFile, categoryService, dataManager);
  }
}
