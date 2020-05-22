package btp.p2p.flashcrowd.init

import peersim.config.Configuration
import peersim.core.CommonState
import peersim.core.Network
import peersim.core.Node
import peersim.dynamics.NodeInitializer
import peersim.edsim.EDSimulator
import peersim.kademlia.KademliaCommonConfig
import peersim.kademlia.KademliaProtocol
import peersim.kademlia.UniformRandomGenerator
import peersim.kademlia.rpc.FindValueOperation
import kotlin.math.floor
import kotlin.math.log10


class InitNode(prefix: String) : NodeInitializer {
    private val pid: Int = Configuration.getPid("$prefix.$PAR_PROT")
    private val dhtPid: Int = Configuration.getPid("$prefix.$PAR_DHT")

    private fun getlevel(rank: Int):Int{

        val ans: Int = floor(log10((base-1)*rank.toDouble() + 1)/(log10(base.toDouble()) + 0.001)).toInt();
        return ans+1
    }
    override fun initialize(n: Node?) {

        val rank = n?.getID();
        val level = rank?.toInt()?.let { getlevel(it) }
//        println(rank.toString() + " " + level.toString())

        val kademliaProtocol2 = n?.getProtocol(dhtPid) as KademliaProtocol
        val msg3 = FindValueOperation(pid, kademliaProtocol2.nodeId, "xyz")
        EDSimulator.add(500, msg3, n, dhtPid)
   }

    companion object{
        var rank = 1
        var base = 4
        private const val PAR_PROT = "protocol"
        private const val PAR_DHT = "dht"
    }
}