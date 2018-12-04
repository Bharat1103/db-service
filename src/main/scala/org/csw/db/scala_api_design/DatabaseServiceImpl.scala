package org.csw.db.scala_api_design

import org.csw.db.scala_api_design.DatabaseService.{SelectQuery, UpdateStatement}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

class DatabaseServiceImpl(db: Database) {
  def query[T](sql: SelectQuery[T]): Future[Seq[T]]        = db.run(sql)
  def update(sqlu: UpdateStatement): Future[Int]           = db.run(sqlu)
  def updateAll(sqlu: List[UpdateStatement]): Future[Unit] = db.run(DBIO.seq(sqlu: _*))
}

object DatabaseService {
  type SelectQuery[T]  = DBIOAction[Seq[T], NoStream, Effect]
  type UpdateStatement = DBIOAction[Int, NoStream, Effect]
}
