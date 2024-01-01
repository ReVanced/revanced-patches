package app.revanced.integrations.twitter.utils.stream

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

object StreamUtils {
    @Throws(IOException::class)
    fun toString(inputStream: InputStream): String {
        ByteArrayOutputStream().use { result ->
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } != -1) {
                result.write(buffer, 0, length)
            }
            return result.toString()
        }
    }

    fun fromString(string: String): InputStream {
        return ByteArrayInputStream(string.toByteArray())
    }
}