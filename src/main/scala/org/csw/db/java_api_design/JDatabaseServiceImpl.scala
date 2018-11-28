package org.csw.db.java_api_design

import java.util
import java.util.concurrent.CompletableFuture

import org.csw.db.scala_api_design.DatabaseServiceImpl
import slick.jdbc.GetResult

import scala.collection.JavaConverters.{
  asScalaBufferConverter,
  seqAsJavaListConverter
}
import scala.compat.java8.FutureConverters.FutureOps
import scala.concurrent.ExecutionContext

class JDatabaseServiceImpl(jdbcDAO: DatabaseServiceImpl)(
    implicit ec: ExecutionContext) {
  def executeQuery[T](
      query: String,
      mapper: java.util.function.Function[DBRow, T]
  ): CompletableFuture[util.List[T]] =
    jdbcDAO
      .executeQuery(query)(GetResult(pr => mapper(new DBRow(pr))))
      .map(_.asJava)
      .toJava
      .toCompletableFuture

  def execute(query: String): CompletableFuture[Int] =
    jdbcDAO.execute(query).toJava.toCompletableFuture

  def execute(queries: java.util.List[String]): CompletableFuture[Unit] =
    jdbcDAO.execute(queries.asScala.toList).toJava.toCompletableFuture

}
