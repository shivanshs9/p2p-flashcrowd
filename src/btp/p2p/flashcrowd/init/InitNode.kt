package btp.p2p.flashcrowd.init

import btp.p2p.flashcrowd.Constants
import btp.p2p.flashcrowd.MsgTypes
import btp.p2p.flashcrowd.protocols.GlobalProt
import peersim.config.Configuration
import peersim.core.Node
import peersim.dynamics.NodeInitializer
import peersim.edsim.EDSimulator
import peersim.kademlia.KademliaProtocol
import peersim.kademlia.rpc.StoreValueOperation
import kotlin.math.floor
import kotlin.math.log10


class InitNode(prefix: String) : NodeInitializer {
    private val pid: Int = Configuration.getPid("$prefix.$PAR_PROT")
    private val dhtPid: Int = Configuration.getPid("$prefix.$PAR_DHT")

    private fun getlevel(rank: Int):Int{

        val ans: Int = floor(log10((base - 1) * rank.toDouble() + 1) / (log10(base.toDouble()) + 0.001)).toInt()
        return ans+1
    }
    override fun initialize(n: Node?) {

        val rank = n?.id
        val level = rank?.toInt()?.let { getlevel(it) } as Int

        val kademliaProtocol = n.getProtocol(dhtPid) as KademliaProtocol
        GlobalProt.setNode(n)
        GlobalProt.add(n.id.toInt(), level)

        val lst = level.let { GlobalProt.getList(it) } as MutableList
        val msg = StoreValueOperation(pid, kademliaProtocol.nodeId, "level_$level", lst)
        msg.type = MsgTypes.STORE_VAL
        EDSimulator.add(50, msg, n, dhtPid)// fertile join

        GlobalProt.addSterile(n.id.toInt(), level)
        val slst = level.let { GlobalProt.getSterileList(it) } as MutableList
        val smsg = StoreValueOperation(pid, kademliaProtocol.nodeId, "s_level_$level", slst)
        smsg.type = MsgTypes.STORE_VAL_STERILE
        EDSimulator.add(Constants.SterileDelay, smsg, n, dhtPid)// sterile join

        GlobalProt.globaladd(n.id.toInt())
    }

    companion object{
        var rank = 1
        var base = 4
        private const val PAR_PROT = "protocol"
        private const val PAR_DHT = "dht"
    }
}