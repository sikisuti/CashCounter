package org.siki.cashcounter.view;

import org.siki.cashcounter.model.Data;
import org.siki.cashcounter.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MainScene {
  @Autowired private DataService service;

  public List<Data> getData() {
    return service.getData();
  }
}
