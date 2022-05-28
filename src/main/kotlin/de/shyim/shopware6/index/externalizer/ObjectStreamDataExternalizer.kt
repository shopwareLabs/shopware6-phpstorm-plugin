package de.shyim.shopware6.index.externalizer

import com.intellij.util.io.DataExternalizer
import java.io.*

@Suppress("UNCHECKED_CAST")
class ObjectStreamDataExternalizer<T : Serializable?> : DataExternalizer<T?> {
    @Throws(IOException::class)
    override fun save(out: DataOutput, value: T?) {
        val stream = ByteArrayOutputStream()
        val output: ObjectOutput = ObjectOutputStream(stream)
        output.writeObject(value)
        out.writeInt(stream.size())
        out.write(stream.toByteArray())
    }

    @Throws(IOException::class)
    override fun read(`in`: DataInput): T? {
        val bufferSize = `in`.readInt()
        val buffer = ByteArray(bufferSize)
        `in`.readFully(buffer, 0, bufferSize)
        val stream = ByteArrayInputStream(buffer)
        val input: ObjectInput = ObjectInputStream(stream)
        var `object`: T? = null
        try {
            `object` = input.readObject() as T
        } catch (ignored: ClassNotFoundException) {
        } catch (ignored: ClassCastException) {
        }
        return `object`
    }
}