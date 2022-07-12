package org.siki.cashcounter.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.repository.DataSourceRaw;
import org.siki.cashcounter.repository.converter.DataSourceMapper;

import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DailyBalanceTest {
  private static ObjectMapper objectMapper;
  @Mock private ConfigurationManager configurationManager;
  private final DataSourceMapper dataSourceMapper = Mappers.getMapper(DataSourceMapper.class);

  private DataManager dataManager;

  @BeforeAll
  static void init() {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @BeforeEach
  void setUp() throws Exception {
    dataManager = new DataManager(configurationManager, dataSourceMapper);
    try (var inputStream = new FileInputStream("src/test/resources/dataForTest.json")) {
      var dataSourceRaw = objectMapper.readValue(inputStream, DataSourceRaw.class);
      wireDependencies(dataSourceRaw);
      dataManager.loadData(dataSourceRaw);
    }
  }

  @Test
  void dayAverageBinging() {
    var dayAverage =
        dataManager.getAllDailyBalances().stream()
            .filter(db -> db.getDate().equals(LocalDate.of(2022, 7, 10)))
            .findFirst()
            .orElseThrow()
            .dayAverageBinding
            .get();

    assertThat(dayAverage).isEqualTo(10);
  }

  private void wireDependencies(DataSourceRaw dataSourceRaw) {
    var allDailyBalances =
        dataSourceRaw.getMonthlyBalances().stream()
            .flatMap(mb -> mb.getDailyBalances().stream())
            .collect(Collectors.toList());
    allDailyBalances.get(0).setDataManager(dataManager);
    for (var i = 1; i < allDailyBalances.size(); i++) {
      var actDailyBalance = allDailyBalances.get(i);
      var prevDailyBalance = allDailyBalances.get(i - 1);
      actDailyBalance.setPrevDailyBalance(prevDailyBalance);
      actDailyBalance.setDataManager(dataManager);
      prevDailyBalance
          .balanceProperty()
          .addListener((observable, oldValue, newValue) -> actDailyBalance.updateBalance());
    }
  }
}
