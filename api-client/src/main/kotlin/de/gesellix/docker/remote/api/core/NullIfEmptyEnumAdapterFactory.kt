package de.gesellix.docker.remote.api.core

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

// inspired by https://github.com/square/moshi/issues/843#issuecomment-584842727
class NullIfEmptyEnumAdapterFactory : JsonAdapter.Factory {

  override fun create(
      type: Type,
      annotations: MutableSet<out Annotation>,
      moshi: Moshi
  ): JsonAdapter<*>? {
//    if (!Types.getRawType(type).isAnnotationPresent(
//        DefaultIfEmpty::class.java)) {
//      return null
//    }
    if (!Types.getRawType(type).isEnum) {
      return null
    }

    val delegate = moshi.nextAdapter<Any>(this, type, annotations)

    return object : JsonAdapter<Any>() {
      override fun fromJson(reader: JsonReader): Any? {
        val nullOrValue = when (val peek = reader.peek()) {
          JsonReader.Token.NULL -> null

          JsonReader.Token.STRING -> {
            val value = reader.readJsonValue() as String
            when {
              value.isEmpty() -> null
              else -> value
            }
          }

          JsonReader.Token.NUMBER -> {
            val value = reader.readJsonValue() as Double
            when {
              value.toInt().compareTo(value) == 0 -> value.toInt().toString()
              value.toLong().compareTo(value) == 0 -> value.toLong().toString()
              else -> value
            }
          }

          JsonReader.Token.BOOLEAN -> reader.readJsonValue() as Boolean

          else -> throw IllegalArgumentException("Token type not supported: $peek")
        }
        return delegate.fromJsonValue(nullOrValue)
      }

      override fun toJson(writer: JsonWriter, value: Any?) {
        return delegate.toJson(writer, value)
      }
    }
  }
}
