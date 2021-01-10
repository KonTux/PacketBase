package de.kontux.packetbase.handler

import de.kontux.packetbase.util.getProtocolState
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.DecoderException

class PacketDecoder : ByteToMessageDecoder() {

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (buf.readableBytes() < 4) {
            //If not even an integer is in the byte buffer, we return as this won't have an id
            return
        }

        val state = ctx.channel().getProtocolState()

        val id = buf.readInt()
        val packet = state.packetRegistry.getPacketById(id)

        try {
            packet.read(buf)
        } catch (e: Exception) {
            throw DecoderException("Exception while decoding packet", e)
        }

        out.add(packet)

        if (buf.readableBytes() > 0) {
            /*If there are still bytes left in the buffer, something is not alright
            This could happen because server and client have different protocol
            versions or specs.
            */
            throw DecoderException("Packet is bigger than expected, ${buf.readableBytes()} left!")
        }

    }
}