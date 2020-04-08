package btp.p2p.flashcrowd.protocols

import peersim.core.Linkable
import peersim.core.Node
import peersim.kademlia.KademliaProtocol
import java.math.BigInteger

class RoutingProtocol(prefix: String) : KademliaProtocol(prefix), Linkable {
    private var neighbours: Array<BigInteger>? = null

    override fun contains(neighbour: Node?): Boolean {
        _sync()
        val neighbourId = (neighbour?.getProtocol(kademliaid) as? KademliaProtocol)?.nodeId ?: return false
        return neighbours?.contains(neighbourId) ?: false
    }

    override fun pack() {
    }

    override fun addNeighbor(neighbour: Node?): Boolean {
        val neighbourId = (neighbour?.getProtocol(kademliaid) as? KademliaProtocol)?.nodeId ?: return false
        return routingTable.runCatching {
            addNeighbour(neighbourId)
            neighbours = (neighbours?.plus(neighbourId) ?: arrayOf(neighbourId))
            true
        }.getOrDefault(false)
    }

    override fun onKill() {
        neighbours = null
    }

    override fun degree(): Int {
        _sync()
        return neighbours?.size ?: 0
    }

    override fun getNeighbor(i: Int): Node? {
        _sync()
        return neighbours?.get(i)?.let { nodeIdtoNode(it) }
    }

    private fun _sync() {
        if (neighbours?.size != routingTable.degree()) {
            neighbours = routingTable.neighbourSet().toTypedArray()
        }
    }
}