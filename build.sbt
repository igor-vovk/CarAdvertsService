name := """carAdvertService"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += Resolver.jcenterRepo

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  filters,
  "com.github.seratch" %% "awscala" % "0.5.+",
  "com.iheart" %% "ficus" % "1.2.3",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

