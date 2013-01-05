import sbt._, Keys._

object build extends Build {
  val main = Project("sbt-tomcat", file(".")).settings(
    organization   := "ro.igstan",
    version        := "0.1.0",
    crossPaths     := false,
    scalaVersion   := "2.9.2",
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    javacOptions  ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
    libraryDependencies ++= Seq(
      "org.apache.tomcat.embed" % "tomcat-embed-core"         % "7.0.34",
      "org.apache.tomcat.embed" % "tomcat-embed-logging-juli" % "7.0.34",
      "org.apache.tomcat.embed" % "tomcat-embed-jasper"       % "7.0.34",
      "org.apache.tomcat"       % "tomcat-catalina"           % "7.0.34",
      "javax.servlet"           % "javax.servlet-api"         % "3.0.1"  % "provided",
      "org.scalatest"          %% "scalatest"                 % "1.8"    % "test"
    )
  ).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
}
