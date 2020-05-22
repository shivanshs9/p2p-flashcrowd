package btp.p2p.flashcrowd.init

import peersim.config.Configuration
import peersim.core.CommonState
import peersim.core.Network
import peersim.core.Node
import peersim.dynamics.NodeInitializer
import peersim.kademlia.KademliaCommonConfig
import peersim.kademlia.KademliaProtocol
import peersim.kademlia.UniformRandomGenerator


class CustomDist(prefix: String) : NodeInitializer {
    private val protocolID: Int
    private val urg: UniformRandomGenerator

    override fun initialize(n: Node?) {

        val tmp = urg.generate()
        (n?.getProtocol(protocolID) as KademliaProtocol).setNode(tmp, n)
    }

    companion object {
        private const val PAR_PROT = "dht"
    }

    init {
        protocolID = Configuration.getPid("$prefix.$PAR_PROT")
        urg = UniformRandomGenerator(KademliaCommonConfig.BITS, CommonState.r)
    }
}
