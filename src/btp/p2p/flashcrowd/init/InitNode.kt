package btp.p2p.flashcrowd.init

import btp.p2p.flashcrowd.messages.NetworkJoin
import btp.p2p.flashcrowd.protocols.GlobalProt
import peersim.config.Configuration
import peersim.core.Node
import peersim.dynamics.NodeInitializer
import peersim.edsim.EDSimulator
import peersim.kademlia.KademliaProtocol

class InitNode(prefix: String) : NodeInitializer {
    private val pid: Int = Configuration.getPid("$prefix.$PAR_PROT")
    private val dhtPid: Int = Configuration.getPid("$prefix.$PAR_DHT")

    override fun initialize(n: Node) {
        val kademliaProtocol = n.getProtocol(dhtPid) as KademliaProtocol
        EDSimulator.add(50, NetworkJoin(pid, kademliaProtocol.nodeId, n.id.toInt()), n, pid)

        GlobalProt.setNode(n)
        GlobalProt.globaladd(n.id.toInt())
    }

    companion object{
        private const val PAR_PROT = "protocol"
        private const val PAR_DHT = "dht"
    }
}