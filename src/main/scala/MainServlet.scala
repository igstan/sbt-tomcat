package ro.igstan.sbt.tomcat

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class MainServlet extends HttpServlet {
  override def doGet(req: HttpServletRequest, res: HttpServletResponse) {
    res.getWriter.write("SERVLET FILE!!!")
  }
}
