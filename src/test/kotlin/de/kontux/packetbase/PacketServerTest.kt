package de.kontux.packetbase

import de.kontux.packetbase.handler.PacketHandler
import de.kontux.packetbase.packet.Packet
import de.kontux.packetbase.packet.PacketListener
import de.kontux.packetbase.protocol.ProtocolState
import de.kontux.packetbase.util.readString
import de.kontux.packetbase.util.writeString
import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import org.junit.Test
import java.net.InetSocketAddress
import java.util.*
import kotlin.concurrent.timer
import kotlin.concurrent.timerTask

class PacketServerTest {

    @Test
    fun test() {
        registerTestPackets()
        val address = InetSocketAddress("localhost", 4444)

        val server = TestServer()
        val client = TestClient()

        server.connect(address, false)
        client.connect(address, false)

        Thread.sleep(100)
    }

    private fun registerTestPackets() {
        TestState.packetRegistry.let {
            it.registerPacket(TestPacket::class)
        }
    }

}

class TestServer : PacketServer() {

    override fun initChannel(channel: Channel) {
        channel.pipeline().addLast(TestHandler())
    }
}

class TestHandler : PacketHandler(TestListener(), TestState) {

    override fun onActive() {
        sendPacket(TestPacket("Hey"))
    }
}

class TestClient : PacketClient() {

    override fun initChannel(channel: Channel) {
        channel.pipeline().addLast(TestHandler())
    }
}

object TestState : ProtocolState()

class TestPacket() : Packet<TestListener> {

    lateinit var text: String

    constructor(text: String) : this() {
        this.text = text
    }

    override fun read(buffer: ByteBuf) {
        this.text = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeString(text)
    }

    override fun handle(listener: TestListener) {
        listener.handleTestPacket(text)
    }
}

class TestListener : PacketListener {

    fun handleTestPacket(text: String) {
        println("Test packet arrived: $text")
    }

}