import sbt._
import sbt.Keys._
import sbt.classpath.ClasspathUtilities.toLoader

object build extends Build {
  val Tomcat = config("tomcat")

  lazy val baseDirectory = SettingKey[File]("base-directory")
  lazy val webappDirectory = SettingKey[File]("webapp-directory")
  lazy val contextPath = SettingKey[String]("context-path")
  lazy val port = SettingKey[Int]("port")

  lazy val start  = TaskKey[Unit]("start")
  lazy val stop   = TaskKey[Unit]("stop")
  lazy val reload = TaskKey[Unit]("reload")

  val main = Project("sbt-tomcat", file(".")).settings(
    baseDirectory in Tomcat   <<= target(_ / "tomcat"),
    webappDirectory in Tomcat <<= baseDirectory(_ / "src" / "main" / "webapp"),
    port in Tomcat             := 8080,
    contextPath in Tomcat      := "/",
    organization               := "ro.igstan",
    version                    := "0.1.0",
    sbtPlugin                  := true,
    crossPaths                 := false,
    scalaVersion               := "2.9.2",
    scalacOptions             ++= Seq("-unchecked", "-deprecation"),
    javacOptions              ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
    libraryDependencies       ++= Seq(
      "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"
    ),

    onUnload in Global <<= (onUnload in Global, streams) {
      (onUnload, streams) => (state) =>
        streams.map { (streams) => tomcat.stop(streams.log); }
        onUnload(state)
    },

    start in Tomcat <<= (compile in Compile, baseDirectory in Tomcat, webappDirectory in Tomcat, classDirectory in Compile, streams, port in Tomcat, contextPath in Tomcat) map {
      (_, baseDirectory, webappDirectory, classDirectory, streams, port, contextPath) => {
        tomcat.start(baseDirectory, webappDirectory, classDirectory, port, contextPath, streams.log)
      }
    },

    stop in Tomcat <<= streams.map { (streams) => tomcat.stop(streams.log) },

    reload in Tomcat <<= (compile in Compile, streams).map {
      (_, streams) => tomcat.reload(streams.log)
    }

  ).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
}

object tomcat {
  import org.apache.catalina.Context
  import org.apache.catalina.loader.WebappLoader
  import org.apache.catalina.startup.Tomcat

  var tomcat: Tomcat = _
  var context: Context = _

  def start(baseDirectory: File, webappDirectory: File, classDirectory: File, port: Int, contextPath: String, logger: Logger) {
    if (tomcat != null) {
      logger.warn("Command ignored. Tomcat is already running.")
    } else {
      tomcat = new Tomcat()
      tomcat.setBaseDir(baseDirectory.getAbsolutePath)
      tomcat.setPort(port)
      context = tomcat.addWebapp(contextPath, webappDirectory.getAbsolutePath)
      context.setReloadable(true)
      val loader = new WebappLoader(toLoader(classDirectory, getClass.getClassLoader))
      loader.addRepository(classDirectory.toURI.toString)
      context.setLoader(loader)

      tomcat.start
    }
  }

  def stop(logger: Logger) {
    if (tomcat != null) {
      tomcat.stop
      tomcat.destroy
      tomcat = null
      context = null
    } else {
      logger.warn("Command ignored. No Tomcat instance is currently running.")
    }
  }

  def reload(logger: Logger) {
    if (tomcat != null) {
      context.reload
    } else {
      logger.warn("Command ignored. No Tomcat instance is currently running.")
    }
  }
}
