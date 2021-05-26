package org.siki.cashcounter.view;

import org.siki.cashcounter.repository.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MainScene {
    @Autowired private DataManager dataManager;

    public void test() {
        dataManager.loadData();
    }
}
