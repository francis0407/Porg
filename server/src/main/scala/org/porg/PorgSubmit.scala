package org.porg

import java.net.URI
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import scala.io.Source

object PorgSubmit {
  def main(args: Array[String]): Unit = {
    val (uri, file) =
      if (args.size > 1)
        (args(0), args(1))
      else
        ("ws://localhost:2411", args(0))

    val client = new SubmitClient(new URI(uri))

    val json = Source.fromFile(file).getLines().mkString("")

    client.send(json)

    while (true) {

    }
  }
}


class SubmitClient(uri: URI) extends WebSocketClient(uri) {

  override def onOpen(handshakedata: ServerHandshake): Unit = {
 }

  override def onMessage(message: String): Unit = {
    System.out.println("received: " + message)
  }

  override def onClose(code: Int, reason: String, remote: Boolean): Unit = { // The codecodes are documented in class org.java_websocket.framing.CloseFrame
    System.out.println("Connection closed by " + (if (remote) "remote peer"
    else "us") + " Code: " + code + " Reason: " + reason)
  }

  override def onError(ex: Exception): Unit = {
    ex.printStackTrace()
}
}