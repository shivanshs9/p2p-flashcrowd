package btp.p2p.flashcrowd.init

import peersim.config.Configuration
import peersim.core.CommonState
import peersim.core.Control
import peersim.core.Network
import peersim.edsim.EDSimulator
import peersim.kademlia.KademliaProtocol
import peersim.kademlia.rpc.FindValueOperation
import peersim.kademlia.rpc.StoreValueOperation

class InitNetwork(val prefix: String) : Control {
    private val pid: Int = Configuration.getPid("$prefix.$PAR_PROTOCOL")
    private val dhtPid: Int = Configuration.getPid("$prefix.$PAR_DHT")

    override fun execute(): Boolean {
//        (0 until Network.size()).forEach {
//            val dhtProtocol = Network.get(it).getProtocol(dhtPid) as DHTProtocol
//            EDSimulator.add(5 * 1000, StoreMessage<String>(dhtProtocol.address, "data $it"), Network.get(it), pid)
//        }
        val node = Network.get(0)
        val kademliaProtocol = node.getProtocol(dhtPid) as KademliaProtocol
        val msg1 = FindValueOperation(pid, kademliaProtocol.nodeId, "xyz")
        val msg2 = StoreValueOperation(pid, kademliaProtocol.nodeId, "xyz", "value")

        val node2 = Network.get(CommonState.r.nextInt(Network.size()))
        val kademliaProtocol2 = node2.getProtocol(dhtPid) as KademliaProtocol
        val msg3 = FindValueOperation(pid, kademliaProtocol2.nodeId, "xyz")
        EDSimulator.add(1000, msg1, node, dhtPid)
        EDSimulator.add(3000, msg2, node, dhtPid)
        EDSimulator.add(5000, msg3, node2, dhtPid)
        return false
    }

    companion object {
        private const val PAR_PROTOCOL = "protocol"
        private const val PAR_DHT = "dht"
    }
}