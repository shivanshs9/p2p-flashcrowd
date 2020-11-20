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
import peersim.kademlia.events.RPCResultPrimitive
import peersim.kademlia.rpc.*
import kotlin.math.pow

class FlashcrowdProtocol(val prefix: String) : EDProtocol {
    private var myId: Int = 0
    private val listOpPid: Int = Configuration.getPid("$prefix.$PAR_LIST_OP")
    private val dhtPid: Int = Configuration.getPid("$prefix.$PAR_DHT")

    // each substream is assumed sterile by default
    val substreams: List<Substream> = (0 until Simulator.noOfSubStreamTrees).map { Substream(it, false) }
    private var cachedNumLevels: Int = 1
    private val tmpStreamToAncestors: MutableMap<Int, MutableList<StreamNodeData>> = mutableMapOf()

    override fun clone(): Any = FlashcrowdProtocol(prefix)

    private fun getFertileTreePlacement(rank: Int): Pair<Int, Int> {
        val nodesPerLevel = listOf(0.0) + (1..cachedNumLevels)
            .map { Simulator.systemCapacity * Simulator.bandwidthK1.toDouble().pow(it - 1) }
            .map { it * Simulator.noOfSubStreamTrees }
        val totalNodes = listOf(0.0) + (1..cachedNumLevels).map { nodesPerLevel[it - 1] + nodesPerLevel[it] }
        var level = 1
        while (true) {
            if (totalNodes[level - 1] < rank && rank <= totalNodes[level]) {
                return level to ((rank - totalNodes[level - 1].toInt()) % Simulator.noOfSubStreamTrees)
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

    private fun fetchGlobalFeedList(kademliaProt: KademliaProtocol, streamId: Int) {
        val key = Constants.KEY_GLOBAL_LIST.format(streamId)
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
                fetchGlobalFeedList(kademliaProt, streamId)

//                if (!GlobalProt.hasJoinedSterile(node.id.toInt())) {
//
//                    val glist = GlobalProt.getgloballist() as MutableList<*>
//                    glist.forEach {
//
//                        if (getlevel(node.id.toInt()) > getlevel(it as Int)) {
//                            val destId = GlobalProt.getNode(it).getProtocol(dhtPid) as KademliaProtocol
//                            val msg =
//                                ConnectionRequest(
//                                    sourceId.nodeId,
//                                    destId.nodeId,
//                                    node.id.toInt(),
//                                    MsgTypes.CONN_REQ_STERILE
//                                )
//                            sourceId.sendMessage(msg, myId)
//                        }
//                    }
//                }
            }
            return
        }
        val parentIdx = ancestorsList.indices.random()
        val connectMsg =
            ConnectionRequest(
                kademliaProt.nodeId,
                ancestorsList[parentIdx].nodeId,
                StreamNodeData(streamId, kademliaProt.nodeId)
            )
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
                        kademliaProt.sendMessage(storeMsg, protocolPid = listOpPid, forceDelay = 0)
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
                kademliaProt.sendMessage(storeMsg, protocolPid = listOpPid, forceDelay = 0)
            }
            // if node has updated its corresponding nodelist then it begins sending connection requests
            is ResultStoreValueOperation -> {
                when (event.type) {
                    MsgTypes.STORE_NUM_LEVELS -> {
                        cachedNumLevels = (event.requestOp as StoreValueOperation).data?.second as Int
                        println("Updating num_level to $cachedNumLevels")
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
                            stream.children.add(event.data)
                            val result = ConnectionResult(event, stream.streamId)
                            kademliaProt.sendMessage(result)
                        } else {
                            val sterileChildren = stream.children.map { it.nodeId }.map {
                                it to (KademliaProtocol.getNode(it)?.getProtocol(myId) as FlashcrowdProtocol)
                            }.filter { (_, childProtocol) ->
                                !childProtocol.substreams[stream.streamId].isFertile
                            }
                            sterileChildren.firstOrNull()?.also { (nodeId, _) ->
                                // case 2: has k children, one of them sterile
                                if (stream.children.removeIf { it.nodeId == nodeId }) {
                                    val disconnectMsg = DisconnectionRequest(kademliaProt.nodeId, nodeId, event.data)
                                    kademliaProt.sendMessage(disconnectMsg)

                                    stream.children.add(event.data)
                                    val result = ConnectionResult(event, stream.streamId)
                                    kademliaProt.sendMessage(result)
                                }
                            } ?: kotlin.run {
                                // case 3: has k children, none is sterile
                                val result = ConnectionResult(event, stream.streamId, RPCResultPrimitive.STATUS_FAIL)
                                kademliaProt.sendMessage(result)
                            }
                        }
                    }
                    MsgTypes.CONN_STERILE_TREE -> {
                        if (stream.children.size < Simulator.bandwidthK1) {
                            stream.children.add(event.data)
                            val result = ConnectionResult(event, stream.streamId)
                            kademliaProt.sendMessage(result)
                        } else {
                            val result = ConnectionResult(event, stream.streamId, RPCResultPrimitive.STATUS_FAIL)
                            kademliaProt.sendMessage(result)
                        }
                    }
                }
            }
            is ConnectionResult -> {
                when (event.status) {
                    RPCResultPrimitive.STATUS_SUCCESS -> {
                        println("Node ${node.id} successfully connected to stream: ${event.data}")

                    }
                    RPCResultPrimitive.STATUS_FAIL -> {
                        val potentialParents = tmpStreamToAncestors[event.data]!!
                        connectToAncestors(kademliaProt, event.data!!, potentialParents)
                    }
                }
            }
        }
    }

    companion object {
        private const val PAR_DHT = "dht"
        private const val PAR_LIST_OP = "list"
    }
}