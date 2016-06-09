import akka.stream.scaladsl.Flow
import akka.NotUsed
import akka.stream.scaladsl.GraphDSL
import akka.stream.scaladsl.Balance
import akka.stream.scaladsl.Merge
import akka.stream.FlowShape
import akka.stream.Attributes

object Utils {

  def balancer[In, Out](worker: Flow[In, Out, Any], workerCount: Int, initialBuffer: Int, maxBuffer: Int): Flow[In, Out, NotUsed] = {
    import GraphDSL.Implicits._

    Flow.fromGraph(GraphDSL.create() { implicit b =>
      val balancer = b.add(Balance[In](workerCount, waitForAllDownstreams = true))
      val merge = b.add(Merge[Out](workerCount))

      for (_ <- 1 to workerCount) {
        balancer ~> worker.async.addAttributes(Attributes.inputBuffer(initialBuffer, maxBuffer)) ~> merge
      }

      FlowShape(balancer.in, merge.out)
    })
  }

}