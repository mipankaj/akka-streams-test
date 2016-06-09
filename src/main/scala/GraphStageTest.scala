

import akka.util.ByteString
import akka.stream.FlowShape
import akka.stream.stage._
import akka.stream.Inlet
import akka.stream.Outlet
import akka.stream.Attributes
import java.security.MessageDigest
import akka.stream.scaladsl.Source
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object GraphStageTest {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("QuickStart")
    implicit val materializer = ActorMaterializer()
    
    
    val longString = "hasra samtra iouiv tillao"
    import scala.collection.immutable.{ Iterable => IIT }

    val list: IIT[String] = longString.split(" ").toList
    val data: scala.collection.immutable.Iterable[(String, ByteString)] = list.map(x => (x, ByteString(x.getBytes)))

    val g = Source(data).via(new DigestCalculator("SHA-256"))
    
    g.runForeach { x => println(x) }

  }

}

class DigestCalculator(algorithm: String) extends GraphStage[FlowShape[(String, ByteString), ByteString]] {

  val in = Inlet[(String, ByteString)]("DigestCalculator.in")
  val out = Outlet[ByteString]("DigestCalculator.out")
  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    val digest = MessageDigest.getInstance(algorithm)

    setHandler(out, new OutHandler {
      override def onPull(): Unit = {
        pull(in)
      }
    })

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        val chunk = grab(in)
        digest.update(chunk._2.toArray)
        pull(in)
      }

      override def onUpstreamFinish(): Unit = {
        emit(out, ByteString(digest.digest()))
        completeStage()
      }
    })
  }

}

