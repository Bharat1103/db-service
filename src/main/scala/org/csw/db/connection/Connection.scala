package org.csw.db.connection

import java.io.File
import java.sql.{DriverManager, ResultSet}

object Connection extends App {
  println("Postgres connector")

  classOf[org.postgresql.Driver]
  val ConnectionString = "jdbc:postgresql://localhost:5432/bharats?user=bharats"
  val connection = DriverManager.getConnection(ConnectionString)

  try {
    val statement = connection.createStatement(
      ResultSet.TYPE_SCROLL_SENSITIVE,
      ResultSet.CONCUR_UPDATABLE
    )

    statement.setFetchSize(10)
    statement.setMaxRows(10)
    val SqlQuery = "SELECT * from images limit 5000"
    val resultSet = statement.executeQuery(SqlQuery)

    println(resultSet.getType)
    println(resultSet.getConcurrency)

    println("blah")
    var count = 0
    while (resultSet.next) {
      println(s"${resultSet.getString(1)}")
      println(count += 1)
    }
//
//    import java.io.FileInputStream
//    import java.sql.PreparedStatement
//
//    var count = 0
//    while (true) {
//      val file = new File("/Users/bharats/Desktop/image.png")
//      val fis = new FileInputStream(file)
//      val ps = connection.prepareStatement("INSERT INTO images VALUES (?, ?)")
//      ps.setString(1, file.getName)
//      ps.setBinaryStream(2, fis, file.length.asInstanceOf[Int])
//      ps.executeUpdate
//      count += 1
//      println(s"$count <<<<<<<<<")
//      fis.close()
//    }

  } finally {
    connection.close()
  }
}
