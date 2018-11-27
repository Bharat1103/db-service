package org.csw.db;


import org.csw.db.connection.JdbcJavaImpl;
import scala.runtime.BoxedUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class JdbcInterop {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        // ****** example 1: select query *******
//
        String movieName = "DDLJ";

        CompletableFuture<List<String>> completableFuture = JdbcJavaImpl.executeQuery("select name from films where name = '" + movieName + "'", pr -> {
            return pr.nextString();
        });

        completableFuture
                .exceptionally(ex -> {
                    // do logging
                    ex.printStackTrace();
                    return new ArrayList<String>();
                })
                .thenAccept(values -> values.forEach(System.out::println));


        // --------------------------------------

        // ****** example 2: select query *******

        CompletableFuture<Integer> completableFuture1 = JdbcJavaImpl.execute("drop table if exists person; create table person(id serial primary key, name varchar(50), address varchar(50))");
        completableFuture1
                .exceptionally(ex -> {
                    // do logging
                    ex.printStackTrace();
                    return 0;
                })
                .thenAccept(System.out::println);

        // --------------------------------------

        // ****** example 3: insert statement query *******

        List<String> queries = new ArrayList<>();
        queries.add("insert into person(name, address) values('Acme, Inc.', '99 Market Street')");
        queries.add("insert into person(name, address) values('Superior Coffee', '1 Party Place')");
        queries.add("insert into person(name, address) values('The High Ground', '100 Coffee Lane')");

        CompletableFuture<BoxedUnit> completableFuture3 = JdbcJavaImpl.executeInserts(queries);
        completableFuture3
                .exceptionally(ex -> {
                    // do logging
                    ex.printStackTrace();
                    return BoxedUnit.UNIT;
                })
                .thenAccept((x) -> System.out.println("All data inserted successfully"));

        // --------------------------------------


        Thread.sleep(10000);
    }
}
