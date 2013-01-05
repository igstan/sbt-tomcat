package ro.igstan.sbt.tomcat

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class MainServlet extends HttpServlet {
  override def doGet(req: HttpServletRequest, res: HttpServletResponse) {
    res.setContentType("text/html")
    res.getWriter.write("<h1>Hello!</h1>")
  }
}
