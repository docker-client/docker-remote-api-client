package de.gesellix.docker.remote.api.core

import com.squareup.moshi.Moshi

object Serializer {

  @JvmStatic
  val moshiBuilder: Moshi.Builder = Moshi.Builder()
    .add(IdResponseAdapter())
    .add(NullIfEmptyEnumAdapterFactory())
//    .add(KotlinJsonAdapterFactory())
    .add(OffsetDateTimeAdapter())
    .add(LocalDateTimeAdapter())
    .add(LocalDateAdapter())
    .add(UUIDAdapter())
    .add(ByteArrayAdapter())
    .add(BigDecimalAdapter())
    .add(BigIntegerAdapter())

  @JvmStatic
  val moshi: Moshi by lazy {
    moshiBuilder.build()
  }
}
