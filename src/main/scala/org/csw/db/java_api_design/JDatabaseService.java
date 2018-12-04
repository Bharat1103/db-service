package org.csw.db.java_api_design;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class JDatabaseService implements IDatabaseService {

    public DSLContext dsl;

    JDatabaseService() {
        dsl = DSL.using("jdbc:postgresql://localhost:5432/bharats?user=bharats");
    }

    public CompletableFuture<Integer> update(String statement, Object... bindings) {
        return dsl
                .query(statement, bindings)
                .executeAsync()
                .toCompletableFuture();
    }

    public CompletableFuture<Void> updateAll(List<QueryWithBindings> statements) {
        return CompletableFuture.allOf(
                statements
                        .stream()
                        .map(x -> dsl.query(x.sql, x.bindings).executeAsync().toCompletableFuture())
                        .collect(Collectors.toList())
                        .toArray(new CompletableFuture[statements.size()])
        );
    }

    public <T> CompletableFuture<List<T>> select(QueryWithBindings query, RecordMapper<Record, T> mapper) {
        return dsl
                .fetchAsync(query.sql, query.bindings.toArray())
                .toCompletableFuture()
                .thenApply(result -> result.map(mapper));
    }
}

class Result{
    Integer i;
    String name;

    public Result(Integer i, String name){
        this.i = i;
        this.name = name;
    }
}