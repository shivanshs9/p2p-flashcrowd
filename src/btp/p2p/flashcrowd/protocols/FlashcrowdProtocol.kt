package btp.p2p.flashcrowd.protocols

import btp.p2p.flashcrowd.ConnectionRequest
import btp.p2p.flashcrowd.init.InitNode
import peersim.config.Configuration
import peersim.core.Network
import peersim.core.Node
import peersim.edsim.EDProtocol
import peersim.edsim.EDSimulator
import peersim.kademlia.events.ProtocolOperation
import peersim.kademlia.KademliaProtocol
import peersim.kademlia.events.RPC
import peersim.kademlia.rpc.*
import kotlin.math.floor
import kotlin.math.log10
import btp.p2p.flashcrowd.MsgTypes
import btp.p2p.flashcrowd.Constants

class FlashcrowdProtocol(val prefix: String) : EDProtocol {
    private var myId: Int = 0
    private val dhtPid: Int = Configuration.getPid("$prefix.$PAR_DHT")
    private val listid: Int = Configuration.getPid("$prefix.$PAR_LIST")
    val childNodes = mutableListOf<Int>();
    val childNodesSterile = mutableListOf<Int>();

    override fun clone(): Any {
        return FlashcrowdProtocol(prefix)
    }

    private fun getlevel(rank: Int):Int{

        val ans: Int = floor(log10((InitNode.base -1)*rank.toDouble() + 1) /(log10(InitNode.base.toDouble()) + 0.001)).toInt();
        return ans+1
    }

    override fun processEvent(node: Node, pid: Int, event: Any?) {
        if (myId == 0) myId = pid
        val global = node.getProtocol(listid) as GlobalProt
//        val rank = node?.getID();

        // Let the DHT Protocol do its stuff
//        (node.getProtocol(dhtPid) as KademliaProtocol).processEvent(node, dhtPid, event)
        when(event){

            is ResultStoreValueOperation ->{

                val level = getlevel(node.getID().toInt())
                val kademliaId = node.getProtocol(dhtPid) as KademliaProtocol

                if(event.type == MsgTypes.STORE_VAL) {
                    val msg = FindValueOperation(myId, kademliaId.nodeId, "level_${level - 1}")
                    msg.type = MsgTypes.FIND_VAL
                    EDSimulator.add(Constants.globalTimeout, msg, node, dhtPid)// fertile join
                }

                if(event.type == MsgTypes.STORE_VAL_STERILE) {
                    val msg = FindValueOperation(myId, kademliaId.nodeId, "s_level_${level - 1}")
                    msg.type = MsgTypes.FIND_VAL_STERILE
                    EDSimulator.add(Constants.globalTimeout, msg, node, dhtPid)// sterile join
                }


            }

            is ResultFindNodeOperation ->{

                val level = getlevel(node.getID().toInt())
                val kademliaId = node.getProtocol(dhtPid) as KademliaProtocol
                val msg = FindValueOperation(myId, kademliaId.nodeId, "level_${level-1}")
                msg.type = MsgTypes.FIND_VAL
                EDSimulator.add(Constants.globalTimeout, msg, node, dhtPid)
            }

            is ResultFindValueOperation ->{

                if(event.type == MsgTypes.FIND_VAL) {

                    val parlist = event.data as MutableList<*>
                    val sourceId = node.getProtocol(dhtPid) as KademliaProtocol

                    parlist.forEach {

                        val destId = global.getNode(it as Int).getProtocol(dhtPid) as KademliaProtocol
                        val msg =
                            ConnectionRequest(sourceId.nodeId, destId.nodeId, node.getID().toInt(), MsgTypes.CONN_REQ)
                        sourceId.sendMessage(msg, myId)
                    }

                    if (!global.hasJoined(node.getID().toInt())) {

                        val level = getlevel(node.getID().toInt())
                        val msg = FindValueOperation(myId, sourceId.nodeId, "level_${level - 1}")
                        msg.type = MsgTypes.FIND_VAL
                        EDSimulator.add(Constants.globalTimeout, msg, node, dhtPid)
                    }
                }

                if(event.type == MsgTypes.FIND_VAL_STERILE) {

                    val parlist = event.data as MutableList<*>
                    val sourceId = node.getProtocol(dhtPid) as KademliaProtocol

                    parlist.forEach {

                        val destId = global.getNode(it as Int).getProtocol(dhtPid) as KademliaProtocol
                        val msg =
                            ConnectionRequest(sourceId.nodeId, destId.nodeId, node.getID().toInt(), MsgTypes.CONN_REQ_STERILE)
                        sourceId.sendMessage(msg, myId)
                    }

                    if (!global.hasJoinedSterile(node.getID().toInt())) {

                        val glist = global.getgloballist() as MutableList<*>
                        glist.forEach {

                            val destId = global.getNode(it as Int).getProtocol(dhtPid) as KademliaProtocol
                            val msg =
                                ConnectionRequest(sourceId.nodeId, destId.nodeId, node.getID().toInt(), MsgTypes.CONN_REQ_STERILE)
                            sourceId.sendMessage(msg, myId)
                        }
                    }
                }
            }

            is ConnectionRequest ->{

                if(event.type==MsgTypes.CONN_REQ) {
                    //println("Node:${node.getID()} received connection request from Node:${event.data}")
                    if (childNodes.size < Constants.K) {

                        val currpar = global.getList(getlevel(node.getID().toInt()))
                        val sourceId = node.getProtocol(dhtPid) as KademliaProtocol
                        val destId = global.getNode(event.data as Int).getProtocol(dhtPid) as KademliaProtocol
                        if(!(currpar?.contains(node.getID().toInt())!!) || global.hasJoined(event.data!!)){

                            val msg = ConnectionRequest(sourceId.nodeId, destId.nodeId, node.getID().toInt(), MsgTypes.CONN_FAILURE)
                            sourceId.sendMessage(msg, myId)
                            return
                        }
                        childNodes.add(event.data!!)
                        global.setJoin(event.data!!)

                        if(childNodes.size == Constants.K){

                            global.remove(node.getID().toInt(),getlevel(node.getID().toInt()))
                            val lst = global.getList(getlevel(node.getID().toInt())) as MutableList
                            val msg = StoreValueOperation(pid, sourceId.nodeId, "level_${getlevel(node.getID().toInt())}", lst)
                            msg.type = MsgTypes.STORE_VAL
                            EDSimulator.add(50, msg, node, dhtPid)
                        }

                        val msg = ConnectionRequest(sourceId.nodeId, destId.nodeId, node.getID().toInt(), MsgTypes.CONN_SUCCESS)
                        sourceId.sendMessage(msg, myId)
                    }
                }

                if(event.type == MsgTypes.CONN_SUCCESS){

                    println("Node ${node.getID()} successfully connected to Node: ${event.data} in its fertile tree")
                }

                if(event.type == MsgTypes.CONN_FAILURE){

                    //println("Node ${node.getID()} failed to connect to Node: ${event.data}")
                }

                if(event.type==MsgTypes.CONN_REQ_STERILE) {

                    if (childNodesSterile.size < Constants.K) {

                        val sourceId = node.getProtocol(dhtPid) as KademliaProtocol
                        val destId = global.getNode(event.data as Int).getProtocol(dhtPid) as KademliaProtocol
                        if(global.hasJoinedSterile(event.data!!)){

                            return
                        }
                        childNodesSterile.add(event.data!!)
                        global.setSterileJoin(event.data!!)

                        val msg = ConnectionRequest(sourceId.nodeId, destId.nodeId, node.getID().toInt(), MsgTypes.CONN_SUCCESS_STERILE)
                        sourceId.sendMessage(msg, myId)
                    }
                }

                if(event.type == MsgTypes.CONN_SUCCESS_STERILE){

                    println("Node ${node.getID()} successfully connected to Node: ${event.data} in its Sterile Tree")
                }
            }
        }
//        if (event is ProtocolOperation<*>) {
//            println(event)
//            println("id: ${event.id}, refId: ${event.refMsgId}, node: ${node.id}")
//            val d = event.data
//            println(d)
//        }
    }

    companion object {
        private const val PAR_DHT = "dht"
        private const val PAR_LIST = "globallist"
    }
}