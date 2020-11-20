package btp.p2p.flashcrowd.protocols

import btp.p2p.flashcrowd.Constants
import btp.p2p.flashcrowd.MsgTypes
import btp.p2p.flashcrowd.Simulator
import btp.p2p.flashcrowd.messages.*
import btp.p2p.flashcrowd.stream.Substream
import peersim.config.Configuration
import peersim.core.Node
import peersim.edsim.EDProtocol
import peersim.edsim.EDSimulator
import peersim.kademlia.KademliaProtocol
import peersim.kademlia.rpc.*
import kotlin.math.pow

class FlashcrowdProtocol(val prefix: String) : EDProtocol {
    private var myId: Int = 0
    private val dhtPid: Int = Configuration.getPid("$prefix.$PAR_DHT")

    // each substream is assumed sterile by default
    private val substreams: List<Substream> = (0 until Simulator.noOfSubStreamTrees).map { Substream(it, false) }
    private var cachedNumLevels: Int = 1
    private val tmpStreamToAncestors: MutableMap<Int, MutableList<StreamNodeData>> = mutableMapOf()

    override fun clone(): Any = FlashcrowdProtocol(prefix)

    private fun getFertileTreePlacement(rank: Int): Pair<Int, Int> {
        val nodesPerLevel = listOf(0) + (1..cachedNumLevels)
            .map { Simulator.systemCapacity * Simulator.bandwidthK1.toDouble().pow(it - 1) }
            .map { it * Simulator.noOfSubStreamTrees }
        val totalNodes = listOf(0) + (1..cachedNumLevels).map { nodesPerLevel[it - 1] + nodesPerLevel[it] }
        var level = 1
        while (true) {
            if (totalNodes[level - 1] < rank && rank <= totalNodes[level]) {
                return level to ((rank - totalNodes[level - 1]) % Simulator.noOfSubStreamTrees)
            }
            level++
        }
    }

    private fun fetchAncestorsList(kademliaProt: KademliaProtocol, streamId: Int, level: Int) {
        val key = Constants.KEY_PARENT_LIST.format(streamId, level - 1)
        val findMsg = FindValueOperation(myId, kademliaProt.nodeId, key).apply {
            type = MsgTypes.FIND_ANCESTORS
        }
        kademliaProt.sendMessage(findMsg)
    }

    private fun connectToAncestors(
        kademliaProt: KademliaProtocol,
        streamId: Int,
        ancestorsList: MutableList<StreamNodeData>
    ) {
        if (ancestorsList.isEmpty()) {
            val stream = substreams[streamId]
            if (stream.isFertile) fetchAncestorsList(kademliaProt, streamId, stream.level)
            else {
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
        val parentIdx = ancestorsList.indices.random()
        val connectMsg =
            ConnectionRequest(kademliaProt.nodeId, ancestorsList[parentIdx].nodeId, ancestorsList[parentIdx])
        kademliaProt.sendMessage(connectMsg)
        ancestorsList.removeAt(parentIdx)
    }

    override fun processEvent(node: Node, pid: Int, event: Any?) {
        val kademliaProt = node.getProtocol(dhtPid) as KademliaProtocol
        if (myId == 0) myId = pid

        when (event) {
            is NetworkJoin -> {
                val rank = event.data!!
                val (fertileLevel, fertileIdx) = getFertileTreePlacement(rank)

                substreams.forEach { stream ->
                    if (stream.streamId == fertileIdx) stream.isFertile = true
                    val level = when {
                        stream.isFertile -> fertileLevel
                        fertileLevel < cachedNumLevels - 1 -> cachedNumLevels - 2
                        else -> cachedNumLevels - 1
                    }
                    stream.level = level

                    if (stream.isFertile) {
                        val data = StreamNodeData(stream.streamId, kademliaProt.nodeId)
                        val storeMsg = ListAppendOperation(myId, kademliaProt.nodeId, data.getLevelKey(level), data)
                        kademliaProt.sendMessage(storeMsg)
                    } else {
                        EDSimulator.add(
                            Constants.SterileDelay,
                            SterileJoin(pid, kademliaProt.nodeId, stream.streamId, level),
                            node,
                            myId
                        )
                    }
                }

                substreams.forEach { stream -> fetchAncestorsList(kademliaProt, stream.streamId, stream.level) }
            }
            is SterileJoin -> {
                val data = StreamNodeData(event.data!!.first, kademliaProt.nodeId)
                val storeMsg = ListAppendOperation(myId, kademliaProt.nodeId, data.getLevelKey(event.data.second), data)
                kademliaProt.sendMessage(storeMsg)
            }
            // if node has updated its corresponding nodelist then it begins sending connection requests
            is ResultStoreValueOperation -> {
                when (event.type) {
                    MsgTypes.STORE_NUM_LEVELS -> {
                        cachedNumLevels = (event.requestOp as StoreValueOperation).data?.second as Int
                    }
                    ListAppendOperation.TYPE_APPEND -> {
                        println("registered in feed forward")
                    }
                }
            }
            is ResultFindNodeOperation -> {
                when (event.type) {
                    MsgTypes.FIND_ANCESTORS -> {
                        // if node doesn't receive parent list, retry after global timeout
                        val key = (event.requestOp as FindValueOperation).data!!
                        val findMsg = FindValueOperation(myId, kademliaProt.nodeId, key).apply {
                            type = MsgTypes.FIND_ANCESTORS
                        }
                        kademliaProt.sendMessage(findMsg, forceDelay = Constants.globalTimeout)
                    }
                }
            }
            is ResultFindValueOperation -> {
                when (event.type) {
                    MsgTypes.FIND_ANCESTORS -> {
                        // node has received parent list
                        val potentialParents = event.data as? MutableList<StreamNodeData> ?: return
                        val streamId = potentialParents.first().streamId
                        tmpStreamToAncestors[streamId] = potentialParents
                        connectToAncestors(kademliaProt, streamId, potentialParents)
                    }
                }
            }
            is ConnectionRequest -> {
                val stream = substreams[event.data!!.streamId]

                when (event.type) {
                    MsgTypes.CONN_FERTILE_TREE -> {
                        // case 1: successful connect
                        if (stream.children.size < Simulator.bandwidthK1) {
                            val result = ConnectionResult(event)
                            kademliaProt.sendMessage(result)
                        } else if (stream.children.size == Simulator.bandwidthK1)
                    }
                    MsgTypes.CONN_STERILE_TREE -> {

                    }
                }
                // fertile connection request
                if (event.type == MsgTypes.CONN_REQ) {
                    //println("Node:${node.getID()} received connection request from Node:${event.data}")
                    val currpar = GlobalProt.getList(getlevel(node.id.toInt()))
                    val sourceId = node.getProtocol(dhtPid) as KademliaProtocol
                    val destId = GlobalProt.getNode(event.data as Int).getProtocol(dhtPid) as KademliaProtocol

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
                        if (childNodesSterile.size > 0) {

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
                        } else {

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
            }
            is ConnectionResult -> {

                if (event.type == MsgTypes.CONN_SUCCESS) {

                    println("Node ${node.id} successfully connected to Node: ${event.data} in its fertile tree")
                }

                if (event.type == MsgTypes.CONN_FAILURE) {

                    //println("Node ${node.getID()} failed to connect to Node: ${event.data}")
                }

                if (event.type == MsgTypes.CONN_REQ_STERILE) {

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

                if (event.type == MsgTypes.CONN_SUCCESS_STERILE) {
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