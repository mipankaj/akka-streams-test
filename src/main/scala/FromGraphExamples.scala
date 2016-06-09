

import scala.concurrent.Await
import akka.stream.SourceShape
import akka.stream.scaladsl.GraphDSL
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Zip
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Broadcast
import akka.stream.FlowShape
import akka.stream.SinkShape
import akka.stream.scaladsl.MergePreferred
import default._

object FromGraphExamples {

  def main(args: Array[String]): Unit = {
    sourceFromGraph()
  }

  def sourceFromGraph() {
    val pairs = Source.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      // prepare graph elements
      val zip = b.add(Zip[Int, Int]())
      def ints = Source.fromIterator(() => Iterator.from(1))

      // connect the graph
      ints.filter(_ % 2 != 0) ~> zip.in0
      ints.filter(_ % 2 == 0) ~> zip.in1

      // expose port
      SourceShape(zip.out)
    })

    val firstPair: Future[(Int, Int)] = pairs.runWith(Sink.head)

    println(Await.result(firstPair, 1.second))

  }

  def flowFromGraph() {
    val pairUpWithToString =
      Flow.fromGraph(GraphDSL.create() { implicit b =>
        import GraphDSL.Implicits._

        // prepare graph elements
        val broadcast = b.add(Broadcast[Int](2))

        val zip = b.add(Zip[Int, String]())

        // connect the graph
        broadcast.out(0).map(identity) ~> zip.in0
        broadcast.out(1).map(_.toString) ~> zip.in1

        // expose ports
        FlowShape(broadcast.in, zip.out)
      })

    val f = pairUpWithToString.runWith(Source(List(1)), Sink.head)
  }

  def sinkFromGraph() {
    Sink.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._
      val broadcast = b.add(Broadcast[Int](2))

      broadcast.out(0) ~> Sink.foreach(println)
      broadcast.out(1) ~> Sink.foreach(println)

      SinkShape.of(broadcast.in)
    })
  }

}