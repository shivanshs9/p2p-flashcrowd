package btp.p2p.flashcrowd.init

import btp.p2p.flashcrowd.messages.StoreMessage
import peersim.config.Configuration
import peersim.core.Control
import peersim.core.Network
import peersim.dht.DHTProtocol
import peersim.edsim.EDSimulator
import peersim.kademlia.KademliaProtocol
import java.math.BigInteger

class InitNetwork(val prefix: String) : Control {
    private val routingPid: Int = Configuration.getPid("$prefix.$PAR_TOPOLOGY")
    private val pid: Int = Configuration.getPid("$prefix.$PAR_PROTOCOL")
    private val dhtPid: Int = Configuration.getPid("$prefix.$PAR_DHT")

    override fun execute(): Boolean {
        (0 until Network.size()).forEach {
            val protocol = Network.get(it).getProtocol(routingPid) as KademliaProtocol
            protocol.setNodeId(BigInteger.valueOf(it.toLong()))
            val dhtProtocol = Network.get(it).getProtocol(dhtPid) as DHTProtocol
            EDSimulator.add(5 * 1000, StoreMessage<String>(dhtProtocol.address, "data $it"), Network.get(it), pid)
        }
        return false
    }

    companion object {
        private const val PAR_TOPOLOGY = "topology"
        private const val PAR_PROTOCOL = "protocol"
        private const val PAR_DHT = "dht"
    }
}