
import default._
import akka.stream.scaladsl.Source
import scala.concurrent.Future
import akka.stream.scaladsl.Sink
import akka.stream.ActorMaterializer
import akka.stream.ActorMaterializerSettings

/**
 * @author pankaj
 */

object PipeliningTest {

  def main(args: Array[String]): Unit = {
    implicit val ec = system.dispatcher
    val mat = ActorMaterializer(
      ActorMaterializerSettings(system)
        .withInputBuffer(
          initialSize = 1,
          maxSize = 1))
      
    val source = Source(1 to 100)
    
    source.mapAsync(1)(x => Future{x}).log("mapAsync", x => println(s"x: $x"))
          .map(_ + 1).async.log("may and async", x => println(s"y: $x"))
          .to(Sink.foreach(println)).run()(mat)
          
//     source.mapAsync(1)(x => Future{x}).async
//          .map(_ + 1).async
//          .to(Sink.foreach(println)).run
  }
}