package org.siki.cashcounter.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.model.DailyBalance;
import org.siki.cashcounter.model.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Slf4j
@AllArgsConstructor
public class DataManager {

    @Autowired private DataHolder dataHolder;
    @Autowired private ConfigurationManager configurationManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void loadData() {
        var dataPath = configurationManager.getStringProperty("DataPath");
        try (var inputStream = new FileInputStream(dataPath)) {
        objectMapper.readValue(inputStream, DataSource.class);
                } catch (IOException e) {
            log.error("Unable to load data file " + dataPath);
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
