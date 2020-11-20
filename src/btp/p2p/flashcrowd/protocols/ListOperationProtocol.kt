package btp.p2p.flashcrowd.protocols

import btp.p2p.flashcrowd.messages.ListAppendOperation
import peersim.core.Node
import peersim.edsim.EDProtocol
import peersim.kademlia.KademliaProtocol
import peersim.kademlia.events.ProtocolOperation
import peersim.kademlia.rpc.FindValueOperation
import peersim.kademlia.rpc.ResultFindValueOperation
import peersim.kademlia.rpc.StoreValueOperation

/**
 * Created by shivanshs9 on 20/11/20.
 */
class ListOperationProtocol(val prefix: String) : EDProtocol {
    private val dhtPid: Int = peersim.config.Configuration.getPid("$prefix.$PAR_DHT")
    private val pendingOps: MutableMap<Long, ProtocolOperation<*>> = linkedMapOf()

    override fun clone(): Any = ListOperationProtocol(prefix)

    override fun processEvent(node: Node, pid: Int, event: Any?) {
        val kademliaProt = node.getProtocol(dhtPid) as KademliaProtocol
        when (event) {
            is ListAppendOperation -> {
                val key = event.data!!.first
                val msg = FindValueOperation(pid, kademliaProt.nodeId, key).apply { refMsgId = event.id }
                kademliaProt.sendMessage(msg)
                pendingOps[msg.id] = event
            }
            is ResultFindValueOperation -> {
                val po = pendingOps[event.refMsgId] ?: return
                if (po is ListAppendOperation) {
                    val lst = event.data as? List<*>
                    val newList = (lst?.toMutableList() ?: mutableListOf()).apply { add(po.data?.second) }
                    val msg = StoreValueOperation(po.protocolPid, kademliaProt.nodeId, po.data!!.first, newList).apply {
                        type = ListAppendOperation.TYPE_APPEND
                    }
                    kademliaProt.sendMessage(msg)
                }
                pendingOps.remove(event.refMsgId)
            }
        }
    }

    companion object {
        private const val PAR_DHT = "dht"
    }
}