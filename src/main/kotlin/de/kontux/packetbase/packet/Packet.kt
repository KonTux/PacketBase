package de.kontux.packetbase.packet

import io.netty.buffer.ByteBuf

interface Packet<L : PacketListener> {

    /**
     * Reads the data from a byte buffer
     */
    fun read(buffer: ByteBuf)

    /**
     * Writes the packet's data to a byte buffer
     */
    fun write(buffer: ByteBuf)

    fun handle(listener: L)
}