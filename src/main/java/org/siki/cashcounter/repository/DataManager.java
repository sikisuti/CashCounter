package org.siki.cashcounter.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.model.MonthlyBalance;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
public class DataManager {

  @Autowired private final ConfigurationManager configurationManager;

  private DataSource dataSource;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public DataManager(ConfigurationManager configurationManager) {
    this.configurationManager = configurationManager;
    objectMapper.registerModule(new JavaTimeModule());
    loadData();
  }

  public List<MonthlyBalance> getMonthlyBalances() {
    return dataSource.monthlyBalances;
  }

  public void loadData() {
    var dataPath = configurationManager.getStringProperty("DataPath");
    try (var inputStream = new FileInputStream(dataPath)) {
      dataSource = objectMapper.readValue(inputStream, DataSource.class);
    } catch (IOException e) {
      log.error("Unable to load data file " + dataPath, e);
    }

    /*ObservableList<DailyBalance> rtnList = FXCollections.observableArrayList();
    int lineCnt = 0;
    DailyBalance prevDailyBalance = null;

    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dataPath), StandardCharsets.UTF_8))) {
        String line;
        while ((line = br.readLine()) != null) {
            lineCnt++;
            DailyBalance db = gsonDeserializer.fromJson(line, DailyBalance.class);
            if (db.getDate().isBefore(LocalDate.now().minusYears(3).withDayOfMonth(1)))
                continue;
            getSavings(db.getDate()).forEach(db::addSaving);
            db.getCorrections().forEach(c -> c.setDailyBalance(db));

            db.getTransactions().forEach(t -> t.setDailyBalance(db));

            if (prevDailyBalance != null) { db.setPrevDailyBalance(prevDailyBalance); }
            rtnList.add(db);
            prevDailyBalance = db;
        }
    } catch (IOException e) {
        throw e;
    } catch (Exception e) {
        throw new JsonDeserializeException(lineCnt, e);
    }

    return rtnList;*/
  }

  @Data
  private static class DataSource {
    private List<MonthlyBalance> monthlyBalances;
  }
}
