package btp.p2p.flashcrowd.protocols

import btp.p2p.flashcrowd.Constants
import btp.p2p.flashcrowd.MsgTypes
import btp.p2p.flashcrowd.init.InitNode
import btp.p2p.flashcrowd.messages.ConnectionRequest
import peersim.config.Configuration
import peersim.core.Node
import peersim.edsim.EDProtocol
import peersim.edsim.EDSimulator
import peersim.kademlia.KademliaProtocol
import peersim.kademlia.rpc.*
import kotlin.math.floor
import kotlin.math.log10

class FlashcrowdProtocol(val prefix: String) : EDProtocol {
    private var myId: Int = 0
    private val dhtPid: Int = Configuration.getPid("$prefix.$PAR_DHT")
    val childNodes = mutableListOf<Int>()
    val childNodesSterile = mutableListOf<Int>()

    override fun clone(): Any {
        return FlashcrowdProtocol(prefix)
    }

    private fun getlevel(rank: Int): Int {

        val ans: Int =
            floor(log10((InitNode.base - 1) * rank.toDouble() + 1) / (log10(InitNode.base.toDouble()) + 0.001)).toInt()
        return ans + 1
    }

    override fun processEvent(node: Node, pid: Int, event: Any?) {
        if (myId == 0) myId = pid
        when(event){

            // if node has updated its corresponding nodelist then it begins sending connection requests
            is ResultStoreValueOperation ->{

                val level = getlevel(node.id.toInt())
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

            // if node doesn't receive parent list, retry after global timeout
            is ResultFindNodeOperation ->{

                val level = getlevel(node.id.toInt())
                val kademliaId = node.getProtocol(dhtPid) as KademliaProtocol
                val msg = FindValueOperation(myId, kademliaId.nodeId, "level_${level-1}")
                msg.type = MsgTypes.FIND_VAL
                EDSimulator.add(Constants.globalTimeout, msg, node, dhtPid)
            }

            // node has received parent list
            is ResultFindValueOperation ->{

                // fertile stream list
                if(event.type == MsgTypes.FIND_VAL) {

                    val parlist = event.data as MutableList<*>
                    val sourceId = node.getProtocol(dhtPid) as KademliaProtocol

                    // send connection request to each node in list
                    parlist.forEach {

                        val destId = GlobalProt.getNode(it as Int).getProtocol(dhtPid) as KademliaProtocol
                        val msg =
                            ConnectionRequest(sourceId.nodeId, destId.nodeId, node.id.toInt(), MsgTypes.CONN_REQ)
                        sourceId.sendMessage(msg, myId)
                    }

                    // if node couldn't connect with any parent in list, then retry
                    if (!GlobalProt.hasJoined(node.id.toInt())) {

                        val level = getlevel(node.id.toInt())
                        val msg = FindValueOperation(myId, sourceId.nodeId, "level_${level - 1}")
                        msg.type = MsgTypes.FIND_VAL
                        EDSimulator.add(Constants.globalTimeout, msg, node, dhtPid)
                    }
                }

                // sterile stream list
                if(event.type == MsgTypes.FIND_VAL_STERILE) {

                    val parlist = event.data as MutableList<*>
                    val sourceId = node.getProtocol(dhtPid) as KademliaProtocol

                    // same as fertile
                    parlist.forEach {
                        val destId = GlobalProt.getNode(it as Int).getProtocol(dhtPid) as KademliaProtocol
                        val msg =
                            ConnectionRequest(
                                sourceId.nodeId,
                                destId.nodeId,
                                node.id.toInt(),
                                MsgTypes.CONN_REQ_STERILE
                            )
                        sourceId.sendMessage(msg, myId)

                    }

                    // try global feedback list if node still hasnt connected
                    if (!GlobalProt.hasJoinedSterile(node.id.toInt())) {

                        val glist = GlobalProt.getgloballist() as MutableList<*>
                        glist.forEach {

                            if (getlevel(node.id.toInt()) > getlevel(it as Int)) {
                                val destId = GlobalProt.getNode(it).getProtocol(dhtPid) as KademliaProtocol
                                val msg =
                                    ConnectionRequest(
                                        sourceId.nodeId,
                                        destId.nodeId,
                                        node.id.toInt(),
                                        MsgTypes.CONN_REQ_STERILE
                                    )
                                sourceId.sendMessage(msg, myId)
                            }
                        }
                    }
                }
            }

            is ConnectionRequest ->{

                // fertile connection request
                if(event.type==MsgTypes.CONN_REQ) {
                    //println("Node:${node.getID()} received connection request from Node:${event.data}")
                    val currpar = GlobalProt.getList(getlevel(node.id.toInt()))
                    val sourceId = node.getProtocol(dhtPid) as KademliaProtocol
                    val destId = GlobalProt.getNode(event.data as Int).getProtocol(dhtPid) as KademliaProtocol

                    // case 1: successful connect
                    if (childNodes.size + childNodesSterile.size < Constants.K) {

                        // check if parent is still in stream list, check if node has joined via some other request
                        if (!(currpar?.contains(node.id.toInt())!!) || GlobalProt.hasJoined(event.data)) {

                            val msg = ConnectionRequest(
                                sourceId.nodeId,
                                destId.nodeId,
                                node.id.toInt(),
                                MsgTypes.CONN_FAILURE
                            )
                            sourceId.sendMessage(msg, myId)
                            return
                        }
                        childNodes.add(event.data)
                        GlobalProt.setJoin(event.data)

                        // remove parent from stream list if it reaches maximum out-degree
                        if (childNodes.size == Constants.K) {
                            GlobalProt.remove(node.id.toInt(), getlevel(node.id.toInt()))
                            val lst = GlobalProt.getList(getlevel(node.id.toInt())) as MutableList
                            // update stream list in DHT
                            val msg = StoreValueOperation(
                                pid,
                                sourceId.nodeId,
                                "level_${getlevel(node.id.toInt())}",
                                lst
                            )
                            msg.type = MsgTypes.STORE_VAL
                            EDSimulator.add(50, msg, node, dhtPid)
                        }

                        // successful connection message
                        val msg = ConnectionRequest(
                            sourceId.nodeId,
                            destId.nodeId,
                            node.id.toInt(),
                            MsgTypes.CONN_SUCCESS
                        )
                        sourceId.sendMessage(msg, myId)
                    }

                    // case 2:
                    else if (childNodes.size + childNodesSterile.size == Constants.K) {

                        // there is sterile child, then pop it from list and connect fertile child instead
                        if(childNodesSterile.size > 0){

                            // invalidate this request if node has joined via some other request
                            if (!(currpar?.contains(node.id.toInt())!!) || GlobalProt.hasJoined(event.data)) {

                                val msg = ConnectionRequest(
                                    sourceId.nodeId,
                                    destId.nodeId,
                                    node.id.toInt(),
                                    MsgTypes.CONN_FAILURE
                                )
                                sourceId.sendMessage(msg, myId)
                                return
                            }

                            // the sterile node disconnects and sends a fresh conn. request
                            val disconn = childNodesSterile[0]
                            childNodesSterile.remove(disconn)
                            GlobalProt.disconnectSterile(disconn)

                            val new_conn_msg = FindValueOperation(
                                myId,
                                (GlobalProt.getNode(disconn).getProtocol(dhtPid) as KademliaProtocol).nodeId,
                                "s_level_${getlevel(disconn) - 1}"
                            )
                            new_conn_msg.type = MsgTypes.FIND_VAL_STERILE
                            EDSimulator.add(
                                Constants.globalTimeout,
                                new_conn_msg,
                                node,
                                dhtPid
                            )// sterile join for disconnected node

                            // add child node to fertile list
                            childNodes.add(event.data)
                            GlobalProt.setJoin(event.data)
                            val msg = ConnectionRequest(
                                sourceId.nodeId,
                                destId.nodeId,
                                node.id.toInt(),
                                MsgTypes.CONN_SUCCESS
                            )
                            sourceId.sendMessage(msg, myId)

                            if (childNodes.size == Constants.K) {
                                GlobalProt.remove(node.id.toInt(), getlevel(node.id.toInt()))
                                val lst = GlobalProt.getList(getlevel(node.id.toInt())) as MutableList
                                val msg = StoreValueOperation(
                                    pid,
                                    sourceId.nodeId,
                                    "level_${getlevel(node.id.toInt())}",
                                    lst
                                )
                                msg.type = MsgTypes.STORE_VAL
                                EDSimulator.add(50, msg, node, dhtPid)
                            }
                        }
                        else{

                            val msg = ConnectionRequest(
                                sourceId.nodeId,
                                destId.nodeId,
                                node.id.toInt(),
                                MsgTypes.CONN_FAILURE
                            )
                            sourceId.sendMessage(msg, myId)
                            return
                        }
                    }
                }

                if(event.type == MsgTypes.CONN_SUCCESS){

                    println("Node ${node.id} successfully connected to Node: ${event.data} in its fertile tree")
                }

                if(event.type == MsgTypes.CONN_FAILURE){

                    //println("Node ${node.getID()} failed to connect to Node: ${event.data}")
                }

                if(event.type==MsgTypes.CONN_REQ_STERILE) {

                    if (childNodes.size + childNodesSterile.size < Constants.K) {

                        val sourceId = node.getProtocol(dhtPid) as KademliaProtocol
                        val destId = GlobalProt.getNode(event.data as Int).getProtocol(dhtPid) as KademliaProtocol
                        if (GlobalProt.hasJoinedSterile(event.data)) {

                            return
                        }
                        childNodesSterile.add(event.data)
                        GlobalProt.setSterileJoin(event.data)

                        val msg = ConnectionRequest(
                            sourceId.nodeId,
                            destId.nodeId,
                            node.id.toInt(),
                            MsgTypes.CONN_SUCCESS_STERILE
                        )
                        sourceId.sendMessage(msg, myId)
                    }
                }

                if(event.type == MsgTypes.CONN_SUCCESS_STERILE){
                    println("Node ${node.id} successfully connected to Node: ${event.data} in its Sterile Tree")
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
    }
}