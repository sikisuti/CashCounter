package org.siki.cashcounter.repository;

import org.siki.cashcounter.model.Data;

import java.util.List;

public interface DataRepository {
    List<Data> find();
}
