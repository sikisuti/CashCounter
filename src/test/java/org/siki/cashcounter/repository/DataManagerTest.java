package org.siki.cashcounter.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.siki.cashcounter.ConfigurationManager;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataManagerTest {
    @Mock ConfigurationManager configurationManager;

    @Test
    void testLoadData() {
        DataHolder dataHolder = new DataHolder();
        when(configurationManager.getStringProperty("DataPath")).thenReturn("test-data.json");
        DataManager dataManager = new DataManager(dataHolder, configurationManager);
        dataManager.loadData();
    }
}