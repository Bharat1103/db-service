package org.csw.db.connection

import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object SlickDBConnection extends App {
  val lines = new ArrayBuffer[Any]()
  def println(s: Any) = lines += s

  import org.csw.db.schema.Schema._

  val db: H2Profile.backend.Database = Database.forConfig("h2mem1")

  try {
    val setup: DBIOAction[Unit, NoStream, Effect.Schema with Effect.Write] =
      DBIO.seq(
        // Create the tables, including primary and foreign keys
        (suppliers.schema ++ coffees.schema).create,
        // Insert some suppliers
        suppliers += (101, "Acme, Inc.", "99 Market Street", "Groundsville", "CA", "95199"),
        suppliers += (49, "Superior Coffee", "1 Party Place", "Mendocino", "CA", "95460"),
        suppliers += (150, "The High Ground", "100 Coffee Lane", "Meadows", "CA", "93966"),
        // Equivalent SQL code:
        // insert into SUPPLIERS(SUP_ID, SUP_NAME, STREET, CITY, STATE, ZIP) values (?,?,?,?,?,?)

        // Insert some coffees (using JDBC's batch insert feature, if supported by the DB)
        coffees ++= Seq(
          ("Colombian", 101, 7.99, 0, 0),
          ("French_Roast", 49, 8.99, 0, 0),
          ("Espresso", 150, 9.99, 0, 0),
          ("Colombian_Decaf", 101, 8.99, 0, 0),
          ("French_Roast_Decaf", 49, 9.99, 0, 0)
        )
        // Equivalent SQL code:
        // insert into COFFEES(COF_NAME, SUP_ID, PRICE, SALES, TOTAL) values (?,?,?,?,?)
      )

    val setupFuture: Future[Unit] = db.run(setup)

    val resultFuture: Future[Unit] = setupFuture
      .flatMap { _ =>
        //#readall
        // Read all coffees and print them to the console
        println("Coffees:")
        db.run(coffees.result)
          .map(_.foreach {
            case (name, supID, price, sales, total) =>
              println(
                "  " + name + "\t" + supID + "\t" + price + "\t" + sales + "\t" + total)
          })
        // Equivalent SQL code:
        // select COF_NAME, SUP_ID, PRICE, SALES, TOTAL from COFFEES
        //#readall

      }
      .flatMap { _ =>
        //#projection
        // Why not let the database do the string conversion and concatenation?
        //#projection
        println("Coffees (concatenated by DB):")
        //#projection
        val q1 = for (c <- coffees)
          yield
            LiteralColumn("  ") ++ c.name ++ "\t" ++ c.supID
              .asColumnOf[String] ++
              "\t" ++ c.price.asColumnOf[String] ++ "\t" ++ c.sales
              .asColumnOf[String] ++
              "\t" ++ c.total.asColumnOf[String]
        // The first string constant needs to be lifted manually to a LiteralColumn
        // so that the proper ++ operator is found

        // Equivalent SQL code:
        // select '  ' || COF_NAME || '\t' || SUP_ID || '\t' || PRICE || '\t' SALES || '\t' TOTAL from COFFEES

        db.stream(q1.result).foreach(println)
        //#projection

      }
      .flatMap { _ =>
        //#join
        // Perform a join to retrieve coffee names and supplier names for
        // all coffees costing less than $9.00
        //#join
        println("Manual join:")
        //#join
        val q2 = for {
          c <- coffees if c.price < 9.0
          s <- suppliers if s.id === c.supID
        } yield (c.name, s.name)
        // Equivalent SQL code:
        // select c.COF_NAME, s.SUP_NAME from COFFEES c, SUPPLIERS s where c.PRICE < 9.0 and s.SUP_ID = c.SUP_ID
        //#join
        db.run(q2.result)
          .map(_.foreach(t => println("  " + t._1 + " supplied by " + t._2)))

      }
      .flatMap { _ =>
        // Do the same thing using the navigable foreign key
        println("Join by foreign key:")
        //#fkjoin
        val q3 = for {
          c <- coffees if c.price < 9.0
          s <- c.supplier
        } yield (c.name, s.name)
        // Equivalent SQL code:
        // select c.COF_NAME, s.SUP_NAME from COFFEES c, SUPPLIERS s where c.PRICE < 9.0 and s.SUP_ID = c.SUP_ID
        //#fkjoin

        db.run(q3.result)
          .map(_.foreach {
            case (s1, s2) => println("  " + s1 + " supplied by " + s2)
          })

      }
    Await.result(resultFuture, Duration.Inf)
    lines.foreach(Predef.println)
    //#setup
  } finally db.close
}
