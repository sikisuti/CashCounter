package org.siki.cashcounter.service;

import org.siki.cashcounter.model.Data;
import org.siki.cashcounter.repository.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataServiceImpl implements DataService {
  @Autowired DataRepository repository;

  @Override
  public List<Data> getData() {
    return repository.find();
  }
}
