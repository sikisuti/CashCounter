package org.siki.cashcounter.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.siki.cashcounter.ConfigurationManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataManagerTest {

  @Mock ConfigurationManager configurationManager;

  @Test
  void testLoadData() throws Exception {
    DataHolder dataHolder = new DataHolder();
    when(configurationManager.getStringProperty("DataPath")).thenReturn("test-data.json");
    DataManager dataManager = new DataManager(dataHolder, configurationManager);
    dataManager.loadData();
    assertEquals(
        1448361,
        dataHolder
            .getDataSource()
            .getMonthlyBalances()
            .get(0)
            .getDailyBalances()
            .get(0)
            .getBalance());
  }
}
