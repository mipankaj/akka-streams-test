

import java.nio.file.Paths
import scala.concurrent.Future
import akka.stream.IOResult
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.ByteString

object FileIOTest {
  def main(args: Array[String]): Unit = {
    import akka.stream.scaladsl._

    implicit val system = ActorSystem("QuickStart")
    implicit val materializer = ActorMaterializer()

    val file = Paths.get("example.csv")

    val foreach: Future[IOResult] = FileIO.fromPath(file)
      .via(Framing.delimiter(
        ByteString("\n"),
        maximumFrameLength = 256,
        allowTruncation = true))
      .map(_.utf8String)
      .to(Sink.foreach(x => println(x)))
      .run()
  }
}