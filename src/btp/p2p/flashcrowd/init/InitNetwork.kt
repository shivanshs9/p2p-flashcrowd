package btp.p2p.flashcrowd.init

import peersim.config.Configuration
import peersim.core.CommonState
import peersim.core.Control
import peersim.core.Network
import peersim.edsim.EDSimulator
import peersim.kademlia.KademliaProtocol
import peersim.kademlia.rpc.*

class InitNetwork(val prefix: String) : Control {
    private val pid: Int = Configuration.getPid("$prefix.$PAR_PROTOCOL")
    private val dhtPid: Int = Configuration.getPid("$prefix.$PAR_DHT")

    override fun execute(): Boolean {

        return false
    }

    companion object {
        private const val PAR_PROTOCOL = "protocol"
        private const val PAR_DHT = "dht"
    }
}