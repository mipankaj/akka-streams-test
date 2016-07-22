

import akka.stream.scaladsl.Source
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.stream.Fusing
import akka.stream.scaladsl.Flow
import scala.concurrent.duration._
import akka.stream.ThrottleMode
import akka.stream.scaladsl.Merge

object AllStagesTest {

  /*
    map
    mapConcat
    statefulMapConcat
    filter
    collect
    grouped
    sliding
    scan
    fold
    drop
    take
    takeWhile
    dropWhile
    recover
    recoverWith
    detach
    throttle
   */
  
  def main(args: Array[String]): Unit = {
    val source = Source(1 to 100).map(i => List(i , i+1))
    val sink = Sink.foreach(println)
    
    implicit val system = ActorSystem("QuickStart")
    implicit val materializer = ActorMaterializer()
    
  /*  
    source.mapConcat { x => x.map( _ + 2) }.runWith(sink)
    
    val f1: List[Int] => List[Int] = x => x.map(_ + 2)
    val f2: List[Int] => List[Int] = x => x.map(_ + 3)
    val f: () => List[Int] => List[Int] = () => {
      if (Math.floor(10).toInt % 2 == 0) f1 else f2
    }
    
    val flow = Flow[List[Int]].mapConcat { x => x.map( _ + 2) }
    val fused = Fusing.aggressive(flow)
    source.via(fused).runWith(sink)
    
    source.statefulMapConcat(f).runForeach(println)
   */
    
//    source.throttle(2, 5.seconds, 3, ThrottleMode.Enforcing).runWith(sink)
    //source.sliding(3, 3).runWith(sink)
    
    
    source.scan(1)((a, b) => b.find { x => x == a }.getOrElse(0)).runWith(sink)
    
    
    val s1 = Source.maybe[Int]
    val s2 = Source(1 to 100)
    
    
    val s = Source.combine(s2, s1)(Merge(_))
    
    
    
  }
}