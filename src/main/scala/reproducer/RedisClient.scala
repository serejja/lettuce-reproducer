package reproducer

import io.lettuce.core.{RedisClient, RedisFuture, SetArgs}
import io.lettuce.core.codec.RedisCodec

import scala.compat.java8.FutureConverters
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConverters._

class RedisClient[K, V](hosts: String, codec: RedisCodec[K, V])(implicit ec: ExecutionContext) {
  private val cache = RedisClient.create(hosts).connect(codec).async()

  private def asScalaFuture[T](f: RedisFuture[T]): Future[T] = {
    FutureConverters.toScala(f.toCompletableFuture)
  }

  def get(key: K): Future[Option[V]] = {
    asScalaFuture(cache.get(key)).map {
      case null => None
      case v => Some(v)
    }
  }

  def getBulk(keys: List[K]): Future[List[V]] = {
    asScalaFuture(cache.mget(keys: _*))
      .map(_.asScala.toList.filter(_.hasValue).map(_.getValue))
  }

  def set(key: K, value: V, ttl: Int): Future[Unit] = {
    asScalaFuture(cache.set(key, value, SetArgs.Builder.ex(ttl))).map(_ => ())
  }
}
