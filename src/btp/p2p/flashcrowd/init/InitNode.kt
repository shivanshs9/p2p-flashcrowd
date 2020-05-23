package btp.p2p.flashcrowd.init

import btp.p2p.flashcrowd.Constants
import btp.p2p.flashcrowd.MsgTypes
import btp.p2p.flashcrowd.protocols.GlobalProt
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
import peersim.kademlia.rpc.StoreValueOperation
import kotlin.math.floor
import kotlin.math.log10


class InitNode(prefix: String) : NodeInitializer {
    private val pid: Int = Configuration.getPid("$prefix.$PAR_PROT")
    private val dhtPid: Int = Configuration.getPid("$prefix.$PAR_DHT")
    private val listid: Int = Configuration.getPid("$prefix.$PAR_LIST")

    private fun getlevel(rank: Int):Int{

        val ans: Int = floor(log10((base-1)*rank.toDouble() + 1)/(log10(base.toDouble()) + 0.001)).toInt();
        return ans+1
    }
    override fun initialize(n: Node?) {

        val rank = n?.getID();
        val level = rank?.toInt()?.let { getlevel(it) } as Int
//        println(rank.toString() + " " + level.toString())

        val kademliaProtocol = n.getProtocol(dhtPid) as KademliaProtocol
        val global = n.getProtocol(listid) as GlobalProt
        global.setNode(n)
        global.add(n.getID().toInt(), level)

        val lst = level?.let { global.getList(it) } as MutableList
        val msg = StoreValueOperation(pid, kademliaProtocol.nodeId, "level_$level", lst)
        msg.type = MsgTypes.STORE_VAL
        EDSimulator.add(50, msg, n, dhtPid)// fertile join

        global.addSterile(n.getID().toInt(), level)
        val slst = level?.let { global.getSterileList(it) } as MutableList
        val smsg = StoreValueOperation(pid, kademliaProtocol.nodeId, "s_level_$level", slst)
        smsg.type = MsgTypes.STORE_VAL_STERILE
        EDSimulator.add(Constants.SterileDelay, smsg, n, dhtPid)// sterile join

        global.globaladd(n.getID().toInt())
   }

    companion object{
        var rank = 1
        var base = 4
        private const val PAR_PROT = "protocol"
        private const val PAR_DHT = "dht"
        private const val PAR_LIST = "globallist"
    }
}