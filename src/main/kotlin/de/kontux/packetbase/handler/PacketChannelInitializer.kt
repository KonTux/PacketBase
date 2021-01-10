package de.kontux.packetbase.handler

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel

class PacketChannelInitializer(private val initializer: ((SocketChannel) -> Unit)? = null) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(channel: SocketChannel) {
        channel.pipeline()?.let {
            it.addLast("decoder", PacketDecoder())
            it.addLast("encoder", PacketEncoder())
        }

        initializer?.invoke(channel)
    }

}