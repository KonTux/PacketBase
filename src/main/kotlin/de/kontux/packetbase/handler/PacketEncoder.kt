package de.kontux.packetbase.handler

import de.kontux.packetbase.packet.Packet
import de.kontux.packetbase.packet.PacketListener
import de.kontux.packetbase.util.getProtocolState
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

class PacketEncoder : MessageToByteEncoder<Packet<*>>() {

    override fun encode(ctx: ChannelHandlerContext, packet: Packet<*>, out: ByteBuf) {
        val state = ctx.channel().getProtocolState()
        val id = state.packetRegistry.getIdByPacket(packet.javaClass)
        out.writeInt(id)
        packet.write(out)
    }
}