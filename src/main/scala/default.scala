

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
object default {
  
  implicit val system = ActorSystem("QuickStart")
    implicit val materializer = ActorMaterializer()
}