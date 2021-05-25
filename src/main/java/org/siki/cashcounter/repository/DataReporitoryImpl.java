package org.siki.cashcounter.repository;

import org.siki.cashcounter.model.Data;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class DataReporitoryImpl implements DataRepository {
    @Override
    public List<Data> find() {
        return Collections.singletonList(Data.builder().name("example").build());
    }
}
