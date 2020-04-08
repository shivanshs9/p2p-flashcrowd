package btp.p2p.flashcrowd.routing

import peersim.core.Linkable
import peersim.core.Node
import peersim.kademlia.KademliaProtocol

class RoutingProtocol(prefix: String) : KademliaProtocol(prefix), Linkable {
    private var neighbours: Array<Node>? = null

    override fun contains(neighbour: Node?): Boolean {
        val neighbourId = (neighbour?.getProtocol(kademliaid) as? KademliaProtocol)?.nodeId ?: return false
        return routingTable.getNeighbours(neighbourId, nodeId).contains(neighbourId)
    }

    override fun pack() {
    }

    override fun addNeighbor(neighbour: Node?): Boolean {
        val neighbourId = (neighbour?.getProtocol(kademliaid) as? KademliaProtocol)?.nodeId ?: return false
        return routingTable.runCatching {
            addNeighbour(neighbourId)
            true
        }.getOrDefault(false)
    }

    override fun onKill() {
        neighbours = null
    }

    override fun degree(): Int = routingTable.degree()

    override fun getNeighbor(i: Int): Node {

    }

    private fun _sync() {

    }
}