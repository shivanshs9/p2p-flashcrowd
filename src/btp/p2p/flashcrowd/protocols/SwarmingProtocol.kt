package btp.p2p.flashcrowd.protocols

import btp.p2p.flashcrowd.Constants
import btp.p2p.flashcrowd.MsgTypes
import btp.p2p.flashcrowd.Simulator
import btp.p2p.flashcrowd.messages.*
import btp.p2p.flashcrowd.stream.MeshSubstream
import peersim.config.Configuration
import peersim.core.Node
import peersim.edsim.EDProtocol
import peersim.edsim.EDSimulator
import peersim.kademlia.KademliaProtocol
import peersim.kademlia.rpc.FindValueOperation
import peersim.kademlia.rpc.ResultFindNodeOperation
import peersim.kademlia.rpc.ResultFindValueOperation
import kotlin.math.pow

/**
 * Created by shivanshs9 on 21/11/20.
 */
@ExperimentalStdlibApi
class SwarmingProtocol(private val prefix: String) : EDProtocol {
    private val flashcrowdPid = Configuration.getPid("$prefix.$PAR_FLASHCROWD")
    private val dhtPid = Configuration.getPid("$prefix.$PAR_DHT")

    private var myPid: Int = 0

    // maps operation IDs to level
    private val pendingOps = mutableMapOf<Long, Int>()
    lateinit var substreams: List<MeshSubstream>
    private var hasSwarmingStarted: Boolean = false

    override fun clone(): Any = SwarmingProtocol(prefix)

    fun isSubstreamInitialized(): Boolean {
        return this::substreams.isInitialized
    }

    private fun fetchPotentialMeshParents(kademliaProt: KademliaProtocol, stream: MeshSubstream) {
        val streamNodeData = StreamNodeData(stream.streamId, kademliaProt.nodeId)
        val currLvl = stream.level
        do {
            val lvl = (currLvl - 2.0.pow(stream.countPendingFetch++)).toInt()
            val key = streamNodeData.getLevelKey(lvl)
            val fetchMsg = FindValueOperation(myPid, kademliaProt.nodeId, key).apply {
                type = MsgTypes.SWM_FETCH_PEERS
            }
            kademliaProt.sendMessage(fetchMsg)
            pendingOps[fetchMsg.id] = lvl
        } while (lvl > 0)
    }

    private fun retryFetchParentsForLevel(kademliaProt: KademliaProtocol, level: Int, requestOp: FindValueOperation) {
        val key = requestOp.data!!
        val fetchMsg = FindValueOperation(myPid, kademliaProt.nodeId, key)
        kademliaProt.sendMessage(fetchMsg)
        pendingOps[fetchMsg.id] = level
    }

    private fun connectToParents(
        kademliaProt: KademliaProtocol,
        stream: MeshSubstream
    ) {
        if (stream.potentialParents.isEmpty()) return
        if (Simulator.bandwidthK2 - Simulator.bandwidthK1 - 1 - stream.parents.size > 0) {
            val idx = stream.potentialParents.indices.random()
            val parentId = stream.potentialParents.removeAt(idx).nodeId
            val selfData = StreamNodeData(stream.streamId, kademliaProt.nodeId)
            val connectRequest = MeshConnectionRequest(kademliaProt.nodeId, parentId, selfData)
            kademliaProt.sendMessage(connectRequest, protocolPid = myPid)
        }
    }

    override fun processEvent(node: Node, pid: Int, event: Any?) {
        myPid = pid
        val flashcrowdProt = node.getProtocol(flashcrowdPid) as FlashcrowdProtocol
        val kademliaProt = node.getProtocol(dhtPid) as KademliaProtocol

        when (event) {
            is SwarmingStart -> {
                if (hasSwarmingStarted) return
                hasSwarmingStarted = true
                substreams = flashcrowdProt.substreams.map { MeshSubstream(it) }
                substreams.forEach {
                    fetchPotentialMeshParents(kademliaProt, it)
                }
            }
            is ResultFindValueOperation -> {
                when (event.type) {
                    MsgTypes.SWM_FETCH_PEERS -> {
                        val lvl = pendingOps.remove(event.refMsgId) ?: return
                        val peers = event.data as? List<StreamNodeData> ?: return
                        if (peers.isEmpty()) retryFetchParentsForLevel(
                            kademliaProt,
                            lvl,
                            event.requestOp as FindValueOperation
                        )
                        else {
                            val stream = substreams[peers.first().streamId]
                            if (stream.countPendingFetch == 0) return
                            if (stream.countPendingFetch-- == 0) {
                                val msg = EndPeersFetch(stream.streamId)
                                EDSimulator.add(0, msg, node, myPid)
                            }
                            if (stream.potentialParents.isEmpty()) {
                                val msg = EndPeersFetch(stream.streamId)
                                EDSimulator.add(Constants.timeoutFetchSwarmingPeers, msg, node, myPid)
                            }
                            stream.potentialParents.addAll(peers)
                        }
                    }
                }
            }
            is ResultFindNodeOperation -> {
                when (event.type) {
                    MsgTypes.SWM_FETCH_PEERS -> {
                        val lvl = pendingOps.remove(event.refMsgId) ?: return
                        retryFetchParentsForLevel(kademliaProt, lvl, event.requestOp as FindValueOperation)
                    }
                }
            }
            is EndPeersFetch -> {
                val stream = substreams[event.streamId]
                stream.countPendingFetch = 0
                connectToParents(kademliaProt, stream)
            }
            is MeshConnectionRequest -> {
                val stream = substreams[event.data!!.streamId]
                // accept all
                stream.children.add(event.data)
                val result = MeshConnectionResult(event, stream.streamId)
                kademliaProt.sendMessage(result, protocolPid = myPid)
            }
            is MeshConnectionResult -> {
                val stream = substreams[event.data!!]
                println("SWARM: connected for stream ${stream.streamId}")
                stream.parents.add(StreamNodeData(stream.streamId, event.destNodeId))
                stream.potentialParents.removeIf { it.nodeId == event.srcNodeId }
                connectToParents(kademliaProt, stream)
            }
        }
    }

    companion object {
        private const val PAR_FLASHCROWD = "flashcrowd"
        private const val PAR_DHT = "dht"
    }
}
