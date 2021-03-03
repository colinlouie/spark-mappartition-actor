/** Standard libraries. */
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt
import scala.util.Random

/** Third-party libraries. */
import akka.pattern.ask

/** Scala construct to store data in.
  *
  * @param key
  *   irrelevant. for illustration purposes only.
  * @param a
  *   irrelevant. for illustration purposes only.
  * @param b
  *   irrelevant. for illustration purposes only.
  * @param timestamp
  *   irrelevant. for illustration purposes only.
  */
case class Record(key: String, a: String, b: String, timestamp: Int)

/** Example Actor to receive a message, and send a reply back. */
class HelloActor extends akka.actor.Actor {

  implicit val system = this.context.system
  implicit val ec = system.getDispatcher

  /** This should print ONCE per Actor creation. */
  println(s">>>>> Instantiating Actor: ${self.path}")

  def receive: Receive = {

    /** Accept a Record case class message. */
    case record: Record => {

      /** Show which specific Actor is processing the inbound Record. */
      println(s"${self.path} is processing: ${record}")

      /** Return value is irrelevant. We only care that the sender gets something back or it will think the call failed.
        */
      sender() ! "pong!"
    }

  } // def receive

} // class HelloActor

object Main {

  /** Create a single ActorSystem. */
  implicit val system = akka.actor.ActorSystem("main")
  implicit val ec = system.getDispatcher

  def main(args: Array[String]): Unit = {
    val appName = "Spark interaction with Actors"

    val sparkConfig = new org.apache.spark.SparkConf()
    val spark = org.apache.spark.sql.SparkSession.builder().config(sparkConfig).appName(appName).getOrCreate()
    val sc = spark.sparkContext

    import spark.implicits._

    /** Create a DataFrame as such:
      *
      * {{{
      * +------+---+---+---------+
      * |   key|  a|  b|timestamp|
      * +------+---+---+---------+
      * |hello1| a1|bb1|       15|
      * |hello2| a2|bb2|       52|
      * |hello3| a3|bb3|       72|
      * |  :      :   :         :|
      * |hello9| a9|bb9|       41|
      * +------+---+---+---------+
      * }}}
      */
    val input = sc
      .parallelize(
        for (i <- 1 until 10) yield {
          Record("hello" + i, "a" + i, "bb" + i, Random.nextInt(100))
        }
      )
      .toDF()

    /** We already had an RDD before we created a DataFrame. Why cast it back as an RDD? This covers the case where you
      * might be reading from something like a Parquet, where you will get a generic SparkSQL Row. This casts it back to
      * our Record case class.
      */
    val postProcessed = input
      .as[Record]
      .rdd
      .repartition(5) // Make it easier to see in the logs that multiple Actors are created.
      .mapPartitionsWithIndex((index, partition) => {

        /** Create a unique Actor per partition. The mapPartitionsWithIndex construct gives us a unique identifier for
          * this purpose.
          */
        val helloClient =
          system.actorOf(
            akka.actor.Props(new HelloActor()),
            "hello" + index
          )

        /** Run all of the blocking I/O calls through at once instead of blocking each call. This is both an
          * anti-pattern to the Spark as well as Akka Actors.
          */
        val replyFutures = partition.map(row => {
          (helloClient ? row)(50 milliseconds)
        })

        /** Wait for all of the Futures to complete. Do NOT run this code in Production. You will want to recover
          * gracefully if any of the Futures timeout.
          */
        val replyFuture = Future sequence replyFutures
        val reply = Await.result(replyFuture, 100 milliseconds)

        /** Tell the  Actor */
        helloClient ! akka.actor.PoisonPill

        /** Return the replies. */
        reply
      })

    /** Perform some action to run the DAG. Couldn't hurt to see the output either :) */
    postProcessed.foreach(println)

    /** Stop the ActorSystem otherwise Spark will never stop. */
    system.terminate()

    /** If it wasn't obvious enough, stop Spark. */
    spark.stop()
  } // def main

} // object Main
