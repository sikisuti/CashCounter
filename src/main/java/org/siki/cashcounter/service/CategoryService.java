package org.siki.cashcounter.service;

import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.repository.DataManager;
import org.springframework.beans.factory.annotation.Autowired;

@RequiredArgsConstructor
public class CategoryService {

  @Autowired private final DataManager dataManager;
}
