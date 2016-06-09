import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.Sink
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.KillSwitches
import default._

object MatTest {

  def main(args: Array[String]): Unit = {
    val flow = Flow[Int].map(_ + 1)
    val source = Source.maybe[Int].via(flow)
    val s = source.viaMat(KillSwitches.single)(Keep.both).toMat(Sink.ignore)(Keep.both).run()
    val ((upstream, switch), downstream) = s

    switch.shutdown()
  }

}