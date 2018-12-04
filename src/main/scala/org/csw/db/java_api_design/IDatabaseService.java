package org.csw.db.java_api_design;

import org.jooq.Record;
import org.jooq.RecordMapper;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IDatabaseService {

    <T> CompletableFuture<List<T>> select(QueryWithBindings query, RecordMapper<Record, T> mapper);

    CompletableFuture<Integer> update(String statement, Object... bindings);

    CompletableFuture<Void> updateAll(List<QueryWithBindings> statements);

}

class QueryWithBindings <T> {
    String sql;
    List<Object> bindings;

    public QueryWithBindings(String sql, Object... bindings) {
        this.sql = sql;
        this.bindings = Arrays.asList(bindings);
    }
}