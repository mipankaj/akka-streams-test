
import scala.concurrent.Future

import akka.Done
import akka.stream.ClosedShape
import akka.stream.scaladsl.Broadcast
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.GraphDSL
import akka.stream.scaladsl.RunnableGraph
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import default.materializer

object StreamTest {

  def main(args: Array[String]): Unit = {

    val source = Source(1 to 100)

    //odd stream
    //event stream

    val odd: Sink[Int, Future[Done]] = Sink.foreach(x => println(s"odd num: $x"))
    val even: Sink[Int, Future[Done]] = Sink.foreach(x => println(s"even num: $x"))

    val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      val broadcast = b.add(Broadcast[Int](2))

      source ~> broadcast.in
      broadcast.out(0) ~> Flow[Int].filter { x => x % 2 == 0 } ~> even
      broadcast.out(1) ~> Flow[Int].filter { x => x % 2 == 1 } ~> odd

      ClosedShape
    })

    g.run()

  }
}