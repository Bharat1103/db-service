package org.csw.db.java_api_design;

import org.csw.db.scala_api_design.DatabaseServiceFactory;
import scala.runtime.BoxedUnit;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        DatabaseServiceFactory jdbcDAOFactory = new DatabaseServiceFactory();
        JDatabaseServiceImpl jdbcDAO = jdbcDAOFactory.jmake();
        complexQueryExample(jdbcDAO);
    }

    private static void complexQueryExample(JDatabaseServiceImpl jdbcDAO) {
        String createTableQuery =
                // create budget
                "CREATE TABLE budget (id SERIAL PRIMARY KEY, movie_id INTEGER, movie_name VARCHAR(10), amount NUMERIC, FOREIGN KEY (movie_id) REFERENCES films(id) ON DELETE CASCADE)";

        String oldName = "movie_1";
        String newName = "DDLJ";

        List<String> queries = new ArrayList<>();
        queries.add("drop table if exists budget");
        queries.add("drop table if exists films");
        queries.add("CREATE TABLE films (id SERIAL PRIMARY KEY, name VARCHAR (10) UNIQUE NOT NULL)");
        queries.add("INSERT INTO films(name) VALUES ('movie_1')");
        queries.add("INSERT INTO films(name) VALUES ('movie_4')");
        queries.add("INSERT INTO films(name) VALUES ('movie_2')");
        queries.add(createTableQuery);
        queries.add("INSERT INTO budget(movie_id, movie_name, amount) VALUES (1, 'movie_1', 5000)");
        queries.add("INSERT INTO budget(movie_id, movie_name, amount) VALUES (2, 'movie_4', 6000)");
        queries.add("INSERT INTO budget(movie_id, movie_name, amount) VALUES (3, 'movie_2', 7000)");
        queries.add("INSERT INTO budget(movie_id, movie_name, amount) VALUES (3, 'movie_2', 3000)");
        queries.add("update budget set movie_name = '"+ newName +"' where movie_name = '"+oldName+"'");
        queries.add("delete from films where name = 'movie_4'");

        jdbcDAO.execute(queries)
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return BoxedUnit.UNIT;
                })
                .thenAccept(unit -> {
                    String newQuery = "SELECT films.name, SUM(budget.amount) " +
                            "FROM films " +
                            "INNER JOIN budget " +
                            "ON films.id = budget.movie_id " +
                            "GROUP BY  films.name";

                    jdbcDAO.executeQuery(newQuery, dbRow -> new WrappedTuple(dbRow.nextString(), dbRow.nextInt()))
                    .thenAccept(tuples -> tuples.forEach(System.out::println));

                });
        }
    }

class WrappedTuple {
    private String name;
    private Integer amt;

    public WrappedTuple(String name, Integer amt) {
        this.name = name;
        this.amt = amt;
    }
}