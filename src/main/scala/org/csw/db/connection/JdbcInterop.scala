package org.csw.db.connection

import slick.jdbc.PostgresProfile.api._
import slick.jdbc.{PostgresProfile, SQLActionBuilder}
import slick.util.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationDouble
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

object JdbcInterop extends Logging {
  val db: PostgresProfile.backend.Database =
    Database.forConfig("postgresConfig")
  def main(args: Array[String]): Unit = {

    // ****** example 1: select query *******

    val movieName = "DDLJ"

    val ss: String = s"select name from films where name = $movieName"

    val sql: SQLActionBuilder =
      sql"select name from films where name = $movieName"

    val action: DBIO[Option[String]] = sql.as[String].headOption
    val resultF: Future[Option[String]] = db.run(action)
    val result = Await.result(resultF, 10.seconds)
    println(result)

//    val sql66: SQLActionBuilder =
//      sql"select name from films where name = 'DDLJ'"
//    val action66: DBIO[Option[String]] = sql66.as[String].headOption
//    val resultF66: Future[Option[String]] = db.run(action66)
//    val result66 = Await.result(resultF66, 10.seconds)
//    println(result66)

    // --------------------------------------

    // ****** example 2: select query *******

    val sql2: SQLActionBuilder = sql"select name from films"
    val action2: DBIO[Seq[String]] = sql2.as[String]
    val resultF2: Future[Seq[String]] = db.run(action2)
    resultF2.onComplete {
      case Success(seq) =>
        seq.foreach { value =>
          // do something
          println(value)
        }
      case Failure(ex) =>
        // do log
        ex.printStackTrace()
    }

    // --------------------------------------

    // ****** example 3: create query *******

    val sql3: DBIO[Int] =
      sqlu"drop table if exists person; create table person(id serial primary key, name varchar(50), address varchar(50))"
    val resultF3: Future[Int] = db.run(sql3)
    resultF3.onComplete {
      case Success(numOfRows) => println(numOfRows)
      case Failure(ex)        => ex.printStackTrace()
    }

    // --------------------------------------

    // ****** example 3: insert statement query *******

    val insertSuppliers: DBIO[Unit] = DBIO.seq(
      // Insert some suppliers
      sqlu"insert into person(name, address) values('Acme, Inc.', '99 Market Street')",
      sqlu"insert into person(name, address) values('Superior Coffee', '1 Party Place')",
      sqlu"insert into person(name, address) values('The High Ground', '100 Coffee Lane')"
    )

    db.run(insertSuppliers).onComplete {
      case Success(_)  => println("All data inserted successfully")
      case Failure(ex) => ex.printStackTrace()
    }

    // --------------------------------------

    Thread.sleep(10000)
  }
}
