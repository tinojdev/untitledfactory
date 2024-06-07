name := "UntitledFactory"

version := "1.0"

scalaVersion := "2.13.14"

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xcheckinit",
  "-encoding",
  "utf8"
)

libraryDependencies += "org.scalafx" %% "scalafx" % "14-R19"

lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _                            => throw new Exception("Unknown platform!")
}

lazy val javaFXModules =
  Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
libraryDependencies ++= javaFXModules.map(m =>
  "org.openjfx" % s"javafx-$m" % "14.0.1" classifier osName
)

assembly / mainClass := Some("main.Main")

assembly / assemblyJarName := "UntitledFactory.jar"

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case _                             => MergeStrategy.first
}
