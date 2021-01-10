package de.kontux.packetbase

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import java.net.SocketAddress

open class PacketClient : ConnectionSide() {

    protected open val bootstrapModifier: ((Bootstrap) -> Unit)? = null
    protected open val workerThreads = 4

    @Synchronized
    final override fun connect(address: SocketAddress, wait: Boolean) {
        val eventLoopGroup = if (EPOLL) EpollEventLoopGroup(workerThreads) else NioEventLoopGroup(workerThreads)

        val bootstrap = Bootstrap()
        bootstrap.group(eventLoopGroup)
        bootstrap.channel(if (EPOLL) EpollSocketChannel::class.java else NioSocketChannel::class.java)
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
        bootstrap.handler(Initializer())

        bootstrapModifier?.invoke(bootstrap)

        this.channel = bootstrap.connect(address).sync().channel()

        channel.closeFuture().let {
            it.addListener {
                eventLoopGroup.shutdownGracefully()
            }

            if (wait) {
                it.syncUninterruptibly()
            }
        }
    }

}