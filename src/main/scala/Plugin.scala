package ro.igstan.sbt.tomcat

import sbt._, Keys._

object Plugin {
  val Tomcat = config("tomcat")

  def tomcatSettings: Seq[sbt.Project.Setting[_]] = {
    import TomcatKeys._

    inConfig(Tomcat)(Seq(
      webappDirectory <<= (baseDirectory in Compile)(_ / "src" / "main" / "webapp"),
      baseDirectory   <<= target(_ / "tomcat"),
      port             := 8080,
      contextPath      := "/",

      onUnload in Global <<= (onUnload in Global, streams) {
        (onUnload, streams) => (state) =>
          streams.map { (streams) => tomcat.stop(streams.log); }
          onUnload(state)
      },

      start in Tomcat <<= (compile in Compile, dependencyClasspath in Compile, resourceDirectory in Compile, unmanagedClasspath in Compile, managedClasspath in Compile, TomcatKeys.baseDirectory in Tomcat, TomcatKeys.webappDirectory in Tomcat, classDirectory in Compile, streams, TomcatKeys.port in Tomcat, TomcatKeys.contextPath in Tomcat) map {
        (_, dependencyClasspath, resourceDirectory, unmanagedClasspath, managedClasspath, baseDirectory, webappDirectory, classDirectory, streams, port, contextPath) => {
          tomcat.start(resourceDirectory +: dependencyClasspath.files, baseDirectory, webappDirectory, classDirectory, port, contextPath, streams.log)
        }
      },

      stop in Tomcat <<= streams.map { (streams) => tomcat.stop(streams.log) },

      reload in Tomcat <<= (compile in Compile, streams).map {
        (_, streams) => tomcat.reload(streams.log)
      }
    ))
  }
}

object TomcatKeys {
  lazy val start           = TaskKey[Unit]("start")
  lazy val stop            = TaskKey[Unit]("stop")
  lazy val reload          = TaskKey[Unit]("reload")
  lazy val baseDirectory   = SettingKey[File]("base-directory")
  lazy val webappDirectory = SettingKey[File]("webapp-directory")
  lazy val contextPath     = SettingKey[String]("context-path")
  lazy val port            = SettingKey[Int]("port")
}

object tomcat {
  import sbt.classpath.ClasspathUtilities.toLoader
  import org.apache.catalina.Context
  import org.apache.catalina.loader.WebappLoader
  import org.apache.catalina.startup.Tomcat

  var tomcat: Tomcat = _
  var context: Context = _

  def start(managedClasspath: Seq[File], baseDirectory: File, webappDirectory: File, classDirectory: File, port: Int, contextPath: String, logger: Logger) {
    if (tomcat != null) {
      logger.warn("Command ignored. Tomcat is already running.")
    } else {
      tomcat = new Tomcat()
      tomcat.setBaseDir(baseDirectory.getAbsolutePath)
      tomcat.setPort(port)
      context = tomcat.addWebapp(contextPath, webappDirectory.getAbsolutePath)
      context.setReloadable(true)
      val loader = new WebappLoader(toLoader(managedClasspath, getClass.getClassLoader))
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
