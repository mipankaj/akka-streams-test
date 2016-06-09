import scala.concurrent.duration.DurationInt

import akka.stream.ClosedShape
import akka.stream.scaladsl.GraphDSL
import akka.stream.scaladsl.RunnableGraph
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Zip
import default.materializer

object TimedFlowTest {

  def main(args: Array[String]): Unit = {
    val source = Source(1 to 100)
    val tickSource = Source.tick(1.second, 1.second, 'Tick);

    val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      val zip = b.add(Zip[Int, Symbol])
      source ~> zip.in0
      tickSource ~> zip.in1
      zip.out ~> Sink.foreach { x: (Int, Symbol) => println(x._1) }

      ClosedShape

    })

    g.run()

  }
}