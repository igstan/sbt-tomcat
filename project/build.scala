import sbt._
import sbt.Keys._
import sbt.classpath.ClasspathUtilities.toLoader
import org.apache.catalina.Context
import org.apache.catalina.loader.WebappLoader
import org.apache.catalina.startup.Tomcat

object build extends Build {
  lazy val workDirectory = SettingKey[File]("work-directory")

  lazy val tomcatStart  = TaskKey[Unit]("tomcat-start")
  lazy val tomcatStop   = TaskKey[Unit]("tomcat-stop")
  lazy val tomcatReload = TaskKey[Unit]("tomcat-reload")

  val main = Project("sbt-tomcat", file(".")).settings(
    workDirectory <<= target(_ / "tomcat"),
    organization   := "ro.igstan",
    version        := "0.1.0",
    sbtPlugin      := true,
    crossPaths     := false,
    scalaVersion   := "2.9.2",
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    javacOptions  ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
    libraryDependencies ++= Seq(
      "javax.servlet"  % "javax.servlet-api" % "3.0.1" % "provided",
      "org.scalatest" %% "scalatest"         % "1.8"   % "test"
    ),

    onUnload in Global <<= (onUnload in Global, streams) {
      (onUnload, streams) => (state) =>
        streams.map { (streams) => tomcat.stop(streams.log); }
        onUnload(state)
    },

    tomcatStart <<= (compile in Compile, workDirectory, baseDirectory, classDirectory in Compile, streams) map {
      (_, workDirectory, baseDirectory, classDirectory, streams) => {
        tomcat.start(workDirectory, baseDirectory, classDirectory, streams.log)
      }
    },

    tomcatStop   <<= streams.map { (streams) => tomcat.stop(streams.log) },
    tomcatReload <<= (compile in Compile, streams).map {
      (_, streams) => tomcat.reload(streams.log)
    }

  ).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
}

object tomcat {
  var tomcat: Tomcat = _
  var context: Context = _

  def start(workDirectory: File, baseDirectory: File, classDirectory: File, logger: Logger) {
    if (tomcat != null) {
      logger.warn("Command ignored. Tomcat is already running.")
    } else {
      tomcat = new Tomcat()
      tomcat.setBaseDir(workDirectory.getAbsolutePath)
      tomcat.setPort(9090)
      val webappDirLocation = baseDirectory / "src" / "main" / "webapp"
      context = tomcat.addWebapp("/", webappDirLocation.getAbsolutePath)
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
      println(context)
      context.reload
    } else {
      logger.warn("Command ignored. No Tomcat instance is currently running.")
    }
  }
}
