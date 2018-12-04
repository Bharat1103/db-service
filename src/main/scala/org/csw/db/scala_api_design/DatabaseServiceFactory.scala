package org.csw.db.scala_api_design

import slick.jdbc.PostgresProfile.api._

class DatabaseServiceFactory {
  val db                          = Database.forConfig("postgresConfig")
  def make(): DatabaseServiceImpl = new DatabaseServiceImpl(db)
//  def jmake(): JDatabaseServiceImpl = new JDatabaseServiceImpl(make())
}
