package ro.igstan.sbt.tomcat

import java.io.File
import org.apache.catalina.startup.Tomcat

object Main {
  def main(args: Array[String]): Unit = {
    val webappDirLocation = "src/main/webapp"
    val tomcat = new Tomcat()

    tomcat.getServer().setParentClassLoader(getClass.getClassLoader)

    tomcat.setPort(9090)
    tomcat.addWebapp("/", new File(webappDirLocation).getAbsolutePath)

    tomcat.start
    tomcat.getServer.await
  }
}
