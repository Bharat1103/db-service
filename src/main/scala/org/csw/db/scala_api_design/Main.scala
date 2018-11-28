package org.csw.db.scala_api_design

import slick.jdbc.GetResult

import scala.concurrent.duration.DurationLong
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object Main {
  def main(args: Array[String]): Unit = {

    val jdbcDAO: DatabaseServiceImpl = new DatabaseServiceFactory().make()

    complexQueryExample(jdbcDAO)
//    simpleQueryExample(jdbcDAO)

    Thread.sleep(10000)
  }

  private def simpleQueryExample(jdbcDAO: DatabaseServiceImpl): Unit = {
    val movieName = "movie_1"
    val query = s"select name from films where name = '$movieName'"
    val resultF: Future[Seq[String]] = jdbcDAO.executeQuery[String](query)
    val result: Seq[String] = Await.result(resultF, 5.seconds)
    println(result)

    val resultWithWrappedNameF: Future[Seq[WrappedName]] =
      jdbcDAO.executeQuery[WrappedName](query)
    val resultWithWrappedName: Seq[WrappedName] =
      Await.result(resultWithWrappedNameF, 5.seconds)
    println(resultWithWrappedName)
  }

  private def complexQueryExample(jdbcDAO: DatabaseServiceImpl): Unit = {
    def createTableQuery(): String =
      // create budget
      """CREATE TABLE budget (
         id SERIAL PRIMARY KEY,
         movie_id INTEGER,
         movie_name VARCHAR(10),
         amount NUMERIC,
         FOREIGN KEY (movie_id) REFERENCES films(id) ON DELETE CASCADE
         )""".stripMargin

    val oldName = "movie_1"
    val newName = "DDLJ"
    jdbcDAO
      .execute(
        List(
          "drop table if exists budget",
          "drop table if exists films",
          "CREATE TABLE films (id SERIAL PRIMARY KEY, name VARCHAR (10) UNIQUE NOT NULL)",
          "INSERT INTO films(name) VALUES ('movie_1')",
          "INSERT INTO films(name) VALUES ('movie_4')",
          "INSERT INTO films(name) VALUES ('movie_2')",
          createTableQuery(),
          "INSERT INTO budget(movie_id, movie_name, amount) VALUES (1, 'movie_1', 5000)",
          "INSERT INTO budget(movie_id, movie_name, amount) VALUES (2, 'movie_4', 6000)",
          "INSERT INTO budget(movie_id, movie_name, amount) VALUES (3, 'movie_2', 7000)",
          "INSERT INTO budget(movie_id, movie_name, amount) VALUES (3, 'movie_2', 3000)",
          s"update budget set movie_name = '$newName' where movie_name = '$oldName'",
          s"delete from films where name = 'movie_4'"
        ))
      .onComplete {
        case Success(value) =>
          val complexQuery =
            """SELECT films.name, SUM(budget.amount)
               FROM films
               INNER JOIN budget
               ON films.id = budget.movie_id
               GROUP BY  films.name
            """.stripMargin

          val resultSet: Future[Seq[(String, Int)]] =
            jdbcDAO.executeQuery[(String, Int)](complexQuery)

          resultSet.foreach(println)
          resultSet.failed.foreach(_.printStackTrace())

        case Failure(ex) => ex.printStackTrace()
      }
  }
}

case class WrappedName(name: String)

object WrappedName {
  implicit val getResult: GetResult[WrappedName] = GetResult(
    r => WrappedName(r.nextString))
}
