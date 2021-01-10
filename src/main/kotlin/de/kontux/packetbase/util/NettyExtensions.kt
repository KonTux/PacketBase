package de.kontux.packetbase.util

import de.kontux.packetbase.protocol.ProtocolState
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.ByteBufOutputStream
import io.netty.channel.Channel
import io.netty.util.AttributeKey
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.nio.charset.Charset

private val STATE_ATTR: AttributeKey<ProtocolState> = AttributeKey.newInstance("state")

fun Channel.getProtocolState() =
    attr(STATE_ATTR).get() ?: throw IllegalStateException("$this does not have a valid protocol state at the moment.")

fun Channel.setProtocolState(state: ProtocolState) = attr(STATE_ATTR).set(state)

fun ByteBuf.writeObject(obj: Serializable) = ObjectOutputStream(ByteBufOutputStream(this)).use { it.writeObject(obj) }

inline fun <reified T : Serializable> ByteBuf.readObject(): T = ObjectInputStream(ByteBufInputStream(this)).use { it.readObject() as T }

fun ByteBuf.writeString(string: String, charset: Charset = Charsets.UTF_8) {
    require(string.length <= Int.MAX_VALUE) { "String may not be longer than ${Int.MAX_VALUE}" }
    writeInt(string.length)
    writeBytes(string.toByteArray(charset))
}

fun ByteBuf.readString(charset: Charset = Charsets.UTF_8): String {
    require(isReadable) { "This byte buffer cannot be read from!" }
    val length = readInt()
    require(readableBytes() >= length) { "Expected string of length $length but there are only ${readableBytes()} bytes left in the buffer!" }
    return (readCharSequence(length, charset)).toString()
}