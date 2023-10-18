package reproducer

import java.util.concurrent.{Executors, Semaphore}
import scala.concurrent.ExecutionContext
import scala.util.Random

object Main {
  def main(args: Array[String]): Unit = {
    implicit val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(16))
    val redisHosts = "redis://localhost:15909"

    val cache = new RedisClient(redisHosts, BrokenCodec)

    (0 to 100).foreach { i =>
      cache
        .set(i.toString, i.toString, 3600)
        .recover { case e: Throwable =>
          e.printStackTrace()
        }
    }

    val sema = new Semaphore(2)
    while (true) {
      sema.acquire()

      val id = Random.nextInt(100).toString
      cache
        .get(id)
        .map { value =>
          value.map { v =>
            if (v != id) {
              println(s"CACHE DISCREPANCY: requested $id got $v")
            }
          }
        }
        .andThen { case _ =>
          sema.release()
        }
    }
  }
}