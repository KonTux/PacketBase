package de.kontux.packetbase

import de.kontux.packetbase.handler.PacketDecoder
import de.kontux.packetbase.handler.PacketEncoder
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.epoll.Epoll
import io.netty.channel.socket.SocketChannel
import java.net.SocketAddress

abstract class ConnectionSide {

    companion object {
        val EPOLL = Epoll.isAvailable()
    }

    protected lateinit var channel: Channel

    /**
     * If the lateinit channel has been initialized
     */
    val isConnected
        get() = this::channel.isInitialized && channel.isActive

    abstract fun connect(address: SocketAddress, wait: Boolean)

    open fun initChannel(channel: Channel) {}

    /**
     * Closes the channel to make the event loop groups shut down and unblock the thread
     */
    @Synchronized
    fun disconnect() {
        check(this::channel.isInitialized) { "Channel has not been initialized yet, cannot close it." }
        check(channel.isOpen) { "Channel is not open, cannot close it." }
        channel.close().sync()
    }

    protected inner class Initializer : ChannelInitializer<SocketChannel>() {

        override fun initChannel(channel: SocketChannel) {
            channel.pipeline()?.let {
                it.addLast("decoder", PacketDecoder())
                it.addLast("encoder", PacketEncoder())
            }

            this@ConnectionSide.initChannel(channel)
        }
    }

}