package btp.p2p.flashcrowd.init

import peersim.config.Configuration
import peersim.core.Control
import peersim.core.Network
import peersim.kademlia.KademliaProtocol
import java.math.BigInteger

class InitNetwork(prefix: String) : Control {
    private val pid: Int = Configuration.getPid("$prefix.$PAR_PROTOCOL")

    override fun execute(): Boolean {
        (0 until Network.size()).forEach {
            val protocol = Network.get(it).getProtocol(pid) as KademliaProtocol
            protocol.setNodeId(BigInteger.valueOf(it.toLong()))
        }
        return false
    }

    companion object {
        private const val PAR_PROTOCOL = "protocol"
    }
}