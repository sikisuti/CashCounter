package org.siki.cashcounter.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.siki.cashcounter.ConfigurationManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class DataManagerTest {

  @Mock private ConfigurationManager configurationManager;
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  static void setUp() {
    objectMapper.registerModule(new JavaTimeModule());
  }

  @Test
  void testLoadData() throws Exception {
    when(configurationManager.getStringProperty("DataPath"))
        .thenReturn(Optional.of("test-data.json"));
    when(configurationManager.getStringProperty("SavingStorePath"))
        .thenReturn(Optional.of("savings.jsn"));
    DataManager dataManager = new DataManager(configurationManager);
    log.info(
        objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(dataManager.getMonthlyBalances()));
    assertEquals(
        1448361, dataManager.getMonthlyBalances().get(0).getDailyBalances().get(0).getBalance());
  }
}
