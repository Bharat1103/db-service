package org.csw.db.scala_api_design

import slick.jdbc.{GetResult, SQLActionBuilder, SetParameter}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

class DatabaseServiceImpl(db: Database) {

  def executeQuery[T](query: String)(
      implicit getResult: GetResult[T]): Future[Seq[T]] =
    db.run(SQLActionBuilder(query, SetParameter.SetUnit).as[T])

  def execute(query: String): Future[Int] =
    db.run(SQLActionBuilder(query, SetParameter.SetUnit).asUpdate)

  def execute(queries: List[String]): Future[Unit] = {
    val queryActions: List[DBIO[Int]] =
      queries.map(SQLActionBuilder(_, SetParameter.SetUnit).asUpdate)
    db.run(DBIO.seq(queryActions: _*))
  }
}
