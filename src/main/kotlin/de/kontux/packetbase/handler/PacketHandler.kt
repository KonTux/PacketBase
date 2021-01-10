package de.kontux.packetbase.handler

import com.sun.istack.internal.logging.Logger
import de.kontux.packetbase.packet.Packet
import de.kontux.packetbase.packet.PacketListener
import de.kontux.packetbase.protocol.ProtocolState
import de.kontux.packetbase.util.setProtocolState
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import java.net.SocketAddress

abstract class PacketHandler(private val listener: PacketListener, private val initialState: ProtocolState) : SimpleChannelInboundHandler<Packet<PacketListener>>() {

    companion object {
        protected val logger: Logger = Logger.getLogger(this::class.java)
    }

    protected lateinit var channel: Channel

    val remoteAddress: SocketAddress
        get() {
            check(isActive) { "Channel is not active, cannot get remote address" }
            return channel.remoteAddress()
        }
    val isActive: Boolean
        get() = this::channel.isInitialized && channel.isActive
    val isOpen: Boolean
        get() = this::channel.isInitialized && channel.isOpen

    final override fun channelActive(ctx: ChannelHandlerContext) {
        this.channel = ctx.channel()
        setState(initialState)
        onActive()
    }

    final override fun channelRead0(ctx: ChannelHandlerContext, packet: Packet<PacketListener>) {
        try {
            packet.handle(listener)
        } catch (e: ClassCastException) {
            throw IllegalStateException("The current packet listener does not seem to match the required listener for packet $packet")
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        logger.warning("Disconnected $remoteAddress: Exception while handling a packet.", cause)
        close()
    }

    fun close() {
        if (isOpen) {
            channel.close()
        }
    }

    fun setState(state: ProtocolState) = channel.setProtocolState(state)

    /**
     * Sends a packet to the channel.
     * @throws IllegalArgumentException if the channel has not been initialized yet.
     */
    fun sendPacket(packet: Packet<*>) {
        check(isActive) { "Channel is not active, cannot send packet!" }
        channel.writeAndFlush(packet)
    }

    abstract fun onActive()
}