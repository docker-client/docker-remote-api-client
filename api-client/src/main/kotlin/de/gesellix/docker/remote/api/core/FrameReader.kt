package de.gesellix.docker.remote.api.core

import de.gesellix.docker.response.Reader
import okio.Buffer
import okio.BufferedSource
import okio.Source
import okio.buffer

class FrameReader(source: Source, private val mediaType: String) : Reader<Frame> {

  private val bufferedSource: BufferedSource = source.buffer()

  private val buffer = Buffer()

  override fun readNext(type: Class<Frame>?): Frame {
    // see https://docs.docker.com/reference/api/engine/version-history/#v142-api-changes
    // see https://github.com/moby/moby/pull/39812
    return if (mediaType == ApiClient.DockerMultiplexedStreamMediaType) {
      // See https://docs.docker.com/engine/api/v1.41/#operation/ContainerAttach for the stream format documentation.
      // header := [8]byte{STREAM_TYPE, 0, 0, 0, SIZE1, SIZE2, SIZE3, SIZE4}

      val streamType = Frame.StreamType.valueOf(bufferedSource.readByte())
      bufferedSource.skip(3)
      val frameSize = bufferedSource.readInt()

      Frame(streamType, bufferedSource.readByteArray(frameSize.toLong()))
    } else {
      var byteCount: Long
      bufferedSource.read(buffer, 8192L).also { byteCount = it }
      if (byteCount < 0) {
        Frame(Frame.StreamType.RAW, null)
      } else {
        Frame(Frame.StreamType.RAW, buffer.readByteArray(byteCount))
      }
    }
  }

  override fun hasNext(): Boolean {
    return try {
      !Thread.currentThread().isInterrupted
//          && bufferedSource.isOpen
          && !bufferedSource.peek().exhausted()
    } catch (_: Exception) {
      return false
    }
  }
}
