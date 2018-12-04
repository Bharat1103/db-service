package org.csw.db.connection

import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import slick.jdbc.{GetResult, JdbcDataSource}
import slick.jdbc.H2Profile.api._
import slick.sql.SqlAction
import slick.util.{AsyncExecutor, ClassLoaderUtil}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object SlickPlainSQLConn extends App {

  var out                      = new ArrayBuffer[String]()
  def println(s: String): Unit = out += s

  //#getresult
  // Case classes for our data
  case class Supplier(id: Int, name: String, street: String, city: String, state: String, zip: String)
  case class Coffee(name: String, supID: Int, price: Double, sales: Int, total: Int)

  // Result set getters
  implicit val getSupplierResult: GetResult[Supplier] = GetResult(
    r => Supplier(r.nextInt, r.nextString, r.nextString, r.nextString, r.nextString, r.nextString)
  )
  implicit val getCoffeeResult = GetResult(r => Coffee(r.<<, r.<<, r.<<, r.<<, r.<<))
  //#getresult

  //===========================================================================================
  def createCoffees: DBIO[Int] =
    sqlu"""create table coffees(
    name varchar not null,
    sup_id int not null,
    price double precision not null,
    sales int not null,
    total int not null,
    foreign key(sup_id) references suppliers(id))"""

  def createSuppliers: DBIO[Int] =
    sqlu"""create table suppliers(
    id int not null primary key,
    name varchar not null,
    street varchar not null,
    city varchar not null,
    state varchar not null,
    zip varchar not null)"""

  def insertSuppliers(): DBIO[Unit] = DBIO.seq(
    // Insert some suppliers
    sqlu"insert into suppliers values(101, 'Acme, Inc.', '99 Market Street', 'Groundsville', 'CA', '95199')",
    sqlu"insert into suppliers values(49, 'Superior Coffee', '1 Party Place', 'Mendocino', 'CA', '95460')",
    sqlu"insert into suppliers values(150, 'The High Ground', '100 Coffee Lane', 'Meadows', 'CA', '93966')"
  )

  def insertCoffees(): DBIO[Unit] = {
    //#bind
    def insert(c: Coffee): DBIO[Int] =
      sqlu"insert into coffees values (${c.name}, ${c.supID}, ${c.price}, ${c.sales}, ${c.total})"
    //#bind

    // Insert some coffees. The SQL statement is the same for all calls:
    // "insert into coffees values (?, ?, ?, ?, ?)"
    //#sequence
    val inserts: Seq[DBIO[Int]] = Seq(
      Coffee("Colombian", 101, 7.99, 0, 0),
      Coffee("French_Roast", 49, 8.99, 0, 0),
      Coffee("Espresso", 150, 9.99, 0, 0),
      Coffee("Colombian_Decaf", 101, 8.99, 0, 0),
      Coffee("French_Roast_Decaf", 49, 9.99, 0, 0)
    ).map(insert)

    val combined: DBIO[Seq[Int]] = DBIO.sequence(inserts)
    combined.map(_.sum)
    //#sequence
  }

  def printAll: DBIO[Unit] =
    // Iterate through all coffees and output them
    sql"select * from coffees".as[Coffee].map { cs =>
      println("Coffees:")
      for (c <- cs)
        println("* " + c.name + "\t" + c.supID + "\t" + c.price + "\t" + c.sales + "\t" + c.total)
    }

  def namesByPrice(price: Double): DBIO[Seq[(String, String)]] = {
    //#sql
    sql"""select c.name, s.name
          from coffees c, suppliers s
          where c.price < $price and s.id = c.sup_id""".as[(String, String)]
    //#sql
  }

  def supplierById(id: Int): DBIO[Seq[Supplier]] =
    sql"select * from suppliers where id = $id".as[Supplier]

  def printParameterized: DBIO[Unit] = {
    // Perform a join to retrieve coffee names and supplier names for
    // all coffees costing less than $9.00
    namesByPrice(9.0).flatMap { l2 =>
      println("Parameterized StaticQuery:")
      for (t <- l2)
        println("* " + t._1 + " supplied by " + t._2)
      supplierById(49).map(s => println(s"Supplier #49: $s"))
    }
  }

  def coffeeByName(name: String): DBIO[Option[Coffee]] = {
    //#literal
    val table = "coffees"
    sql"select * from #$table where name = $name".as[Coffee].headOption
    //#literal
  }

  def coffeeUpdate(newName: String, oldName: String): DBIO[Int] = {
    //#literal
    val table = "coffees"
    sqlu"update coffees set name = $newName where name = $oldName"
    //#literal
  }

  val oldName = "movie_1 ; drop table coffees;"
  val newName = "DDLJ"

  def deleteCoffee(name: String): DBIO[Int] =
    sqlu"delete from coffees where name = $name"
  //===========================================================================================

  val db = Database.forConfig("postgresConfig")

//  val connectionPool                   -> "HikariCP",
//  val properties.cachePrepStmts        -> "true",
//  val properties.prepStmtCacheSize     -> "20000",
//  val properties.prepStmtCacheSqlLimit -> "100000",
//
//  (name: String, minThreads: Int, maxThreads: Int, queueSize: Int, maxConnections: Int = Integer.MAX_VALUE, keepAliveTime: Duration = 1.minute,
//  registerMbeans: Boolean = false)

  try {
    val f: Future[_] = {

      val a: DBIO[Unit] = DBIO.seq(
        sqlu"drop table if exists coffees",
        sqlu"drop table if exists suppliers",
        createSuppliers,
        createCoffees,
        coffeeUpdate("blah;drop table coffees;", "Colombian"),
//        insertSuppliers(),
//        insertCoffees(),
//        printAll,
//        printParameterized,
//        coffeeByName("Colombian").map { s =>
//          println(s"Coffee Colombian: $s")
//        },
//        deleteCoffee("Colombian").map { rows =>
//          println(s"Deleted $rows rows")
//        },
//        coffeeByName("Colombian").map { s =>
//          println(s"Coffee Colombian: $s")
//        }
      )
      db.run(a)
    }
    Await.result(f, Duration.Inf)
  } finally db.close

  out.foreach(Console.out.println)

}
