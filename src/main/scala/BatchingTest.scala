

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.GraphDSL
import akka.stream.SinkShape
import scala.concurrent.duration._
import akka.stream.scaladsl.Zip
import scala.collection.mutable.ListBuffer
import akka.stream.FlowShape
import akka.stream.Outlet
import default._

object BatchingTest {

  def main(args: Array[String]): Unit = {
    val source = Source(1 to 100)

    val slowSink = Sink.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      val tickSource = Source.tick(1.millis, 5.seconds, 'Tick)

      val zipper = b.add(Zip[Symbol, List[Int]])
      tickSource ~> zipper.in0
      zipper.out ~> Sink.foreach { x: (Symbol, List[Int]) => println(x._2) }

      SinkShape.of(zipper.in1)
    })

    val timedFlow = Flow.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      val tickSource = Source.tick(1.seconds, 60.seconds, 'Tick)
      val zipper = b.add(Zip[Symbol, List[Int]])
      tickSource ~> zipper.in0
      zipper.out
      FlowShape(zipper.in1, zipper.out)
    })

    source.batch(Int.MaxValue, a => List(a)) {
      (list, b) => b :: list
    }.via(timedFlow).map(x => x._2.max)

    val s = Source(List(List(1, 2, 3), List(4), List(5)))

    def seed(a: List[Int]): Map[String, List[Int]] = {
      a.groupBy { x => x.toString }
    }

  }

}