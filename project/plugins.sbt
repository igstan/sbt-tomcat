addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.0")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.2.0")

libraryDependencies ++= Seq(
  "org.apache.tomcat"       % "tomcat-catalina"           % "7.0.34",
  "org.apache.tomcat.embed" % "tomcat-embed-core"         % "7.0.34",
  "org.apache.tomcat.embed" % "tomcat-embed-logging-juli" % "7.0.34",
  "org.apache.tomcat.embed" % "tomcat-embed-jasper"       % "7.0.34"
)

libraryDependencies <+= (sbtVersion)("org.scala-sbt" % "classpath" % _ % "provided")
