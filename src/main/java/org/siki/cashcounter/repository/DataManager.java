package org.siki.cashcounter.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.model.DataSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
public class DataManager {

  @Autowired private final DataHolder dataHolder;
  @Autowired private final ConfigurationManager configurationManager;

  private final ObjectMapper objectMapper = new ObjectMapper();

  public DataManager(DataHolder dataHolder, ConfigurationManager configurationManager) {
    this.dataHolder = dataHolder;
    this.configurationManager = configurationManager;
    objectMapper.registerModule(new JavaTimeModule());
  }

  public void loadData() {
    var dataPath = configurationManager.getStringProperty("DataPath");
    try (var inputStream = new FileInputStream(dataPath)) {
      dataHolder.setDataSource(objectMapper.readValue(inputStream, DataSource.class));
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
}
