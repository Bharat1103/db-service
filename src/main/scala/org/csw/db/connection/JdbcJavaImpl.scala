package org.csw.db.connection

import java.util
import java.util.concurrent.CompletableFuture

import slick.jdbc.PostgresProfile.api._
import slick.jdbc._
import slick.util.Logging

import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters.FutureOps
import scala.concurrent.ExecutionContext.Implicits.global

object JdbcJavaImpl extends Logging {
  val db: PostgresProfile.backend.Database =
    Database.forConfig("postgresConfig")

  def executeQuery[T](
      query: String,
      mapper: java.util.function.Function[PositionedResult, T]
  ): CompletableFuture[util.List[T]] = {
    val actionBuilder: SQLActionBuilder =
      SQLActionBuilder(query, SetParameter.SetUnit)
    val gr: GetResult[T] = GetResult(pr => mapper(pr))
    val action: DBIO[Seq[T]] = actionBuilder.as[T](gr)
    db.run(action)
      .map(seq => seqAsJavaList(seq))
      .toJava
      .toCompletableFuture
  }

  def execute(query: String): CompletableFuture[Integer] = {
    val builder = SQLActionBuilder(query, SetParameter.SetUnit)
    val action: DBIO[Int] = builder.asUpdate
    db.run(action)
      .map(x => x.asInstanceOf[java.lang.Integer])
      .toJava
      .toCompletableFuture
  }

  def executeInserts(queries: util.List[String]): CompletableFuture[Unit] = {
    val list: Seq[DBIO[Int]] = queries.asScala.map(query =>
      SQLActionBuilder(query, SetParameter.SetUnit).asUpdate)
    val action = DBIO.seq(list: _*)
    db.run(action).toJava.toCompletableFuture
  }
}
