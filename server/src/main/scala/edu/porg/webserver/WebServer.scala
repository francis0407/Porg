package edu.porg.webserver

import akka.http.scaladsl.server.{HttpApp, Route}

class WebServer extends HttpApp {

  override protected def routes: Route = {
    get {
      concat(
        path("") {
          getFromResource("static/index.html")
        },
        pathPrefix("api") {
          concat(
            path("abc") {
              complete("hello")
            },
            path("abd" / IntNumber) { p =>
              println(p)
              complete("886")
            }
          )
        },
        pathPrefix("") {
          getFromResourceDirectory("static")
        }
      )
    }
  }

}
