package btp.p2p.flashcrowd.controller

import btp.p2p.flashcrowd.Constants
import btp.p2p.flashcrowd.MsgTypes
import btp.p2p.flashcrowd.Simulator
import peersim.config.Configuration
import peersim.core.Control
import peersim.core.Network
import peersim.edsim.EDSimulator
import peersim.kademlia.KademliaProtocol
import peersim.kademlia.rpc.StoreValueOperation
import kotlin.math.ceil
import kotlin.math.log10

/**
 * Created by shivanshs9 on 20/11/20.
 */
class FlashcrowdController(private val prefix: String) : Control {
    private val dhtPid: Int = Configuration.getPid("$prefix.$PAR_DHT")
    private val flashcrowdPid: Int = Configuration.getPid("$prefix.$PAR_FLASHCROWD")

    override fun execute(): Boolean {
        val kademliaProt = Network.get(0).getProtocol(dhtPid) as KademliaProtocol
        val flashcrowdSize = Network.size()
        val numLevel =
            ceil(log10(flashcrowdSize * (Simulator.bandwidthK1 - 1.0) / Simulator.systemCapacity) / log10(Simulator.bandwidthK1.toDouble()))
        val storeMsg =
            StoreValueOperation(flashcrowdPid, kademliaProt.nodeId, Constants.KEY_NUM_LEVELS, numLevel).apply {
                type = MsgTypes.STORE_NUM_LEVELS
            }
        EDSimulator.add(0, storeMsg, Network.get(0), dhtPid)
        return false
    }

    companion object {
        private const val PAR_FLASHCROWD = "protocol"
        private const val PAR_DHT = "dht"
    }
}