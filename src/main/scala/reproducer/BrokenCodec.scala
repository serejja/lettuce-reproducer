package reproducer

import io.lettuce.core.codec.{RedisCodec, StringCodec}

import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import scala.util.Random

object BrokenCodec extends RedisCodec[String, String] {
  private val exceptionThrown = new AtomicBoolean(false)

  override def decodeKey(bytes: ByteBuffer): String = StringCodec.UTF8.decodeKey(bytes)

  override def decodeValue(bytes: ByteBuffer): String = StringCodec.UTF8.decodeValue(bytes)

  override def encodeKey(key: String): ByteBuffer = StringCodec.UTF8.encodeKey(key)

  override def encodeValue(value: String): ByteBuffer = {
    if (!exceptionThrown.getAndSet(true)) {
      throw new NullPointerException("boom!")
    } else {
      StringCodec.UTF8.encodeValue(value)
    }
  }
}
