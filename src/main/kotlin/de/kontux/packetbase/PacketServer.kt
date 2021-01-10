package de.kontux.packetbase

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import java.net.SocketAddress

open class PacketServer : ConnectionSide() {

    protected open val bootstrapModifier: ((ServerBootstrap) -> Unit)? = null
    protected open val workerThreads = 4

    /**
     * Builds the server bootstrap and binds it to the given address.
     * This will block the thread until the event loop groups have been shut down
     */
    @Synchronized
    final override fun connect(address: SocketAddress, wait: Boolean) {
        val bossGroup = if (EPOLL) EpollEventLoopGroup() else NioEventLoopGroup()
        val workerGroup = if (EPOLL) EpollEventLoopGroup(workerThreads) else NioEventLoopGroup(workerThreads)

        val bootstrap = ServerBootstrap()
        bootstrap.group(bossGroup, workerGroup)
        bootstrap.channel(if (EPOLL) EpollServerSocketChannel::class.java else NioServerSocketChannel::class.java)
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true)
        bootstrap.childHandler(Initializer())
        bootstrapModifier?.invoke(bootstrap)

        this.channel = bootstrap.bind(address).sync().channel()

        channel.closeFuture().let {
            it.addListener {
                workerGroup.shutdownGracefully()
                bossGroup.shutdownGracefully()
            }
            if (wait) {
                it.syncUninterruptibly()
            }
        }
    }
}