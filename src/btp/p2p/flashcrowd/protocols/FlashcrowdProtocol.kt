package btp.p2p.flashcrowd.protocols

import btp.p2p.flashcrowd.messages.BaseMessage
import btp.p2p.flashcrowd.messages.StoreMessage
import peersim.config.Configuration
import peersim.core.Node
import peersim.dht.DHTProtocol
import peersim.edsim.EDProtocol

class FlashcrowdProtocol(val prefix: String) : EDProtocol {
    private var myId: Int = 0
    private val dhtPid: Int = Configuration.getPid("$prefix.$PAR_DHT")
    private val routingPid: Int = Configuration.getPid("$prefix.$PAR_TOPOLOGY")

    override fun clone(): Any {
        return FlashcrowdProtocol(prefix)
    }

    override fun processEvent(node: Node, pid: Int, event: Any?) {
        if (myId == 0) {
            myId = pid
        }

        // Let the DHT Protocol do its stuff
        (node.getProtocol(dhtPid) as DHTProtocol).processEvent(node, dhtPid, event)
        when (event as? BaseMessage<*>) {
            is StoreMessage -> println("store message: ${(event as BaseMessage<*>).data}")
        }
    }

    companion object {
        private const val PAR_DHT = "dht"
        private const val PAR_TOPOLOGY = "topology"
    }
}