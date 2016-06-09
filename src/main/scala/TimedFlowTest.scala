

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import scala.concurrent.duration._
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.GraphDSL
import akka.stream.FlowShape
import akka.stream.scaladsl.Zip
import akka.stream.ClosedShape
import akka.stream.scaladsl.RunnableGraph
import akka.stream.scaladsl.Sink

object TimedFlowTest {
  
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("QuickStart")
    implicit val materializer = ActorMaterializer()
    
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