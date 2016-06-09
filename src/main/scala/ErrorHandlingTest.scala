

import akka.stream.scaladsl.Source
import scala.concurrent.duration._
import akka.stream.scaladsl.GraphDSL
import akka.stream.scaladsl.Zip
import akka.stream.SourceShape
import akka.stream.scaladsl.Sink
import default._
import scala.concurrent.Future
import akka.stream.ActorAttributes
import akka.stream.Supervision
import scala.concurrent.ExecutionContext
import akka.stream.scaladsl.RunnableGraph
import akka.stream.ClosedShape
import akka.stream.scaladsl.Unzip
import ActorAttributes.supervisionStrategy
import Supervision._
import akka.stream.scaladsl.Flow
import akka.stream.SinkShape
import akka.stream.scaladsl.Broadcast
    
object ErrorHandlingTest {

  def main(args: Array[String]): Unit = {
    import system.dispatcher
    val source = Source(1 to 100)
    val tickSource = Source.tick(1.second, 2.second, 'Tick);

    val s = Source.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      val zip = b.add(Zip[Int, Symbol])
      source ~> zip.in0
      tickSource ~> zip.in1

      SourceShape(zip.out)

    })

    //example1(s)
    example2(s)
  }
  def example1(s: Source[(Int, Symbol), _])(implicit dispatcher: ExecutionContext) {

    

    s.map(x => { println(s"x: $x"); x })
      .mapAsync(2)(y => Future { println(s"y: $y"); if (y._1 % 4 == 0) throw new Exception("abort"); y }).withAttributes(supervisionStrategy(resumingDecider)).runWith(Sink.foreach(println))

  }
  
  def example2(s: Source[(Int, Symbol), _])(implicit dispatcher: ExecutionContext) {
    val flow = Flow[Int].groupedWithin(2, 1.second).mapConcat(x => x).mapAsync(2)(y => Future { println(s"y: $y"); if (y % 4 == 0) throw new Exception("abort"); y }).withAttributes(supervisionStrategy(resumingDecider))
    
    val sink = Sink.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._
      
      val unzip = b.add(Unzip[Int, Symbol])
      //s ~> unzip.in
      unzip.out0.map(x => { println(s"x: $x"); x }).via(flow)
      .to(Sink.foreach(println))
      
      unzip.out1.to(Sink.ignore)
      
      SinkShape(unzip.in)
    })
    
    val source = Source.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._
      val bd = b.add(Broadcast[(Int, Symbol)](2))
      
      s ~> Utils.balancer(Flow[(Int, Symbol)].map(x => { println(s"balanced $x"); x}), 2, 1, 1) ~> bd.in
      bd.out(0) ~> Sink.ignore
      
      SourceShape(bd.out(1))
    })
    
    source.to(sink).run()
  }
}