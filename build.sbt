name := "Multipaxos"

version := "1.0"

scalaVersion := "2.10.3"

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-actors" % _)

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-swing" % _)
