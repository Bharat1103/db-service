name := "db-service"

version := "0.1"

scalaVersion := "2.12.7"

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "42.2.5",
  "com.h2database" % "h2" % "1.0.60",
  "com.typesafe.slick" %% "slick" % "3.2.3",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.3",
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0",
  "org.jooq" % "jooq" % "3.11.7"
)