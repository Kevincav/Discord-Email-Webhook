ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "DiscordEmailWebhook",
    libraryDependencies ++= Seq(
      "javax.mail" % "mail" % "1.4.7",
      "org.apache.commons" % "commons-email" % "1.5",
      "org.jsoup" % "jsoup" % "1.15.4",
      "org.scala-lang.modules" %% "scala-xml" % "2.1.0",
      "club.minnced" % "discord-webhooks" % "0.8.2",
      "com.amazonaws" % "aws-lambda-java-events" % "3.11.0",
      "com.github.davidmoten" % "aws-lightweight-client-java" % "0.1.14",
    ),
  )