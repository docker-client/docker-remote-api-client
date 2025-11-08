package de.gesellix.docker.remote.api.core

import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import de.gesellix.docker.client.core.json.CustomObjectAdapterFactory
import okio.Source
import okio.buffer
import java.io.IOException

class JsonChunksReader<T> @JvmOverloads constructor(
  source: Source,
  private val moshi: Moshi = Moshi.Builder().add(CustomObjectAdapterFactory()).build()
) : Reader<T> {
  private val reader: JsonReader = JsonReader.of(source.buffer())

  init {
    // For transfer-encoding: chunked:
    // allows repeated `readNext` calls to consume
    // a complete stream of JSON chunks (delimited or not).
    this.reader.isLenient = true
  }

  @Throws(IOException::class)
  override fun readNext(type: Class<T>): T {
    return moshi.adapter<T>(type).fromJson(reader)!!
//        return reader.readJsonValue()
  }

  @Throws(IOException::class)
  override fun hasNext(): Boolean {
    return !Thread.currentThread().isInterrupted && reader.hasNext()
  }
}
