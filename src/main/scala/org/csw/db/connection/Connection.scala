package org.csw.db.connection

import java.sql.{Connection, DriverManager, ResultSet}

object Connection extends App {
  println("Postgres connector")

  classOf[org.postgresql.Driver]
  val ConnectionString =
    "jdbc:postgresql://localhost:5432/DATABASE_NAME?user=DATABASE_USER"
  val connection: Connection = DriverManager.getConnection(ConnectionString)
  try {
    val statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                                               ResultSet.CONCUR_READ_ONLY)

    val SqlQuery = "SELECT (degrees(long(p)), degrees(lat(p))) from points;"
    val resultSet = statement.executeQuery(SqlQuery)

    while (resultSet.next) {
      println(s"${resultSet.getString(1)}")
    }

  } finally {
    connection.close()
  }
}
