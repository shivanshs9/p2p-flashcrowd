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

class FlashcrowdProtocol(val prefix: String) : EDProtocol {
    private var myId: Int = 0
    private val dhtPid: Int = Configuration.getPid("$prefix.$PAR_DHT")
    private val listid: Int = Configuration.getPid("$prefix.$PAR_LIST")
    val childNodes = mutableListOf<Int>();

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
                val msg = FindValueOperation(myId, kademliaId.nodeId, "level_${level-1}")
                msg.type = MsgTypes.FIND_VAL
                EDSimulator.add(3000, msg, node, dhtPid)
            }

            is ResultFindNodeOperation ->{

                val level = getlevel(node.getID().toInt())
                val kademliaId = node.getProtocol(dhtPid) as KademliaProtocol
                val msg = FindValueOperation(myId, kademliaId.nodeId, "level_${level-1}")
                msg.type = MsgTypes.FIND_VAL
                EDSimulator.add(3000, msg, node, dhtPid)
            }

            is ResultFindValueOperation ->{

                val parlist = event.data as MutableList<*>
                println(parlist)
                val sourceId = node.getProtocol(dhtPid) as KademliaProtocol

                parlist.forEach{

                    val destId = global.getNode(it as Int).getProtocol(dhtPid) as KademliaProtocol
                    val msg = ConnectionRequest(sourceId.nodeId, destId.nodeId, node.getID().toInt(), MsgTypes.CONN_REQ)
                    sourceId.sendMessage(msg,myId)
                }

                if(!global.hasJoined(node.getID().toInt())){

                    val level = getlevel(node.getID().toInt())
                    val msg = FindValueOperation(myId, sourceId.nodeId, "level_${level-1}")
                    msg.type = MsgTypes.FIND_VAL
                    EDSimulator.add(2000, msg, node, dhtPid)
                }
            }

            is ConnectionRequest ->{

                if(event.type==MsgTypes.CONN_REQ) {
                    //println("Node:${node.getID()} received connection request from Node:${event.data}")
                    if (event != null && childNodes.size < 4) {

                        val currpar = global.getList(getlevel(node.getID().toInt()))
                        if(!(currpar?.contains(node.getID().toInt())!!) || global.hasJoined(event.data!!)){

                            val sourceId = node.getProtocol(dhtPid) as KademliaProtocol
                            val destId = global.getNode(event.data as Int).getProtocol(dhtPid) as KademliaProtocol
                            val msg = ConnectionRequest(sourceId.nodeId, destId.nodeId, node.getID().toInt(), MsgTypes.CONN_FAILURE)
                            sourceId.sendMessage(msg, myId)
                            return
                        }
                        val sourceId = node.getProtocol(dhtPid) as KademliaProtocol
                        childNodes.add(event.data!!)
                        global.setJoin(event.data!!)
                        if(childNodes.size == 4)    global.remove(node.getID().toInt(),getlevel(node.getID().toInt()))
                        val destId = global.getNode(event.data as Int).getProtocol(dhtPid) as KademliaProtocol
                        val msg = ConnectionRequest(sourceId.nodeId, destId.nodeId, node.getID().toInt(), MsgTypes.CONN_SUCCESS)
                        sourceId.sendMessage(msg, myId)
                    }
                }

                if(event.type == MsgTypes.CONN_SUCCESS){

                    println("Node ${node.getID()} successfully connected to Node: ${event.data}")
                }

                if(event.type == MsgTypes.CONN_FAILURE){

                    println("Node ${node.getID()} failed to connect to Node: ${event.data}")
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