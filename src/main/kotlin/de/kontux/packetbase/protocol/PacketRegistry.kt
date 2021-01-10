package de.kontux.packetbase.protocol

import de.kontux.packetbase.packet.Packet
import de.kontux.packetbase.packet.PacketListener
import kotlin.reflect.KClass

class PacketRegistry {

    private val byId = HashMap<Int, Class<out Packet<out PacketListener>>>()
    private val byPacket = HashMap<Class<out Packet<out PacketListener>>, Int>()

    /**
     * Creates a new instance of the packet registered with this id.
     * @throws IllegalArgumentException if no packet with this id is registered
     */
    fun getPacketById(id: Int): Packet<out PacketListener> = byId.getOrElse(id) {
        throw IllegalArgumentException("Bad packet id: $id")
    }.newInstance()

    /**
     * Gets the id of a packet type.
     * @throws IllegalArgumentException if the packet is not registered
     */
    fun getIdByPacket(packetClass: Class<out Packet<out PacketListener>>) = byPacket.getOrElse(packetClass) {
        throw IllegalArgumentException("${packetClass.simpleName} is not registered!")
    }

    /**
     * Registers a packet and uses the registration order as IDs
     */
    fun registerPacket(packetClass: Class<out Packet<out PacketListener>>) {
        val id = byId.size
        registerPacket(packetClass, id)
    }

    /**
     * Registers a packet and uses the registration order as IDs
     */
    fun registerPacket(packetClass: KClass<out Packet<out PacketListener>>) = registerPacket(packetClass.java)

    /**
     * Registers a packet with the given id.
     * This will override the previous registration if
     * this id already exists.
     *
     * @throws IllegalArgumentException if the packet class
     * does not have a public and empty constructor
     */
    fun registerPacket(packetClass: Class<out Packet<out PacketListener>>, id: Int) {
        checkConstructors(packetClass)
        byId[id] = packetClass
        byPacket[packetClass] = id
    }

    private fun checkConstructors(packetClass: Class<out Packet<out PacketListener>>) {
        for (constructor in packetClass.constructors) {
            if (constructor.parameterCount == 0) {
                return
            }
        }

        throw IllegalArgumentException("${packetClass.simpleName} does not have an empty and public constructor")
    }

}