package de.gesellix.docker.remote.api.core

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.math.BigInteger

class BigIntegerAdapter {

  @ToJson
  fun toJson(value: BigInteger): String {
    return value.toString()
  }

  @FromJson
  fun fromJson(value: String): BigInteger {
    return BigInteger(value)
  }
}
