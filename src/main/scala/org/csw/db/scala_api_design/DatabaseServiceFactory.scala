package org.csw.db.scala_api_design

import org.csw.db.java_api_design.JDatabaseServiceImpl
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global

class DatabaseServiceFactory {
  val db = Database.forConfig("postgresConfig")
  def make(): DatabaseServiceImpl = new DatabaseServiceImpl(db)
  def jmake(): JDatabaseServiceImpl = new JDatabaseServiceImpl(make())
}
