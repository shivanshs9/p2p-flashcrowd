package btp.p2p.flashcrowd.controller

import btp.p2p.flashcrowd.activeNodes
import btp.p2p.flashcrowd.protocols.FlashcrowdProtocol
import btp.p2p.flashcrowd.stream.Substream
import peersim.config.Configuration
import peersim.core.CommonState
import peersim.core.Control
import peersim.core.Network

class Statistics(prefix: String) : Control {

    private val flashcrowdid: Int = Configuration.getPid("$prefix.$PAR_FLASHCROWD")

    //    private val swarmingid: Int = Configuration.getPid("$prefix.$PAR_SWARMING")
    private val dhtPid: Int = Configuration.getPid("$prefix.$PAR_DHT")

    fun Boolean.toInt() = if (this) 1 else 0

    private fun isConnected(substream: List<Substream>): Pair<Int, Int> {

        val connectedStreams = substream.filter { (it.parents.size > 0) }
        return Pair((connectedStreams.size > 0).toInt(), (connectedStreams.size == substream.size).toInt())
    }


    override fun execute(): Boolean {

        println("\n--- Statistics at Time: ${CommonState.getTime()} ---\n")

        println("Nodes in Overlay: ${Network.size()}")

//      churn rate
        val churnRatio = 1 - ((Network::class.activeNodes().size.toDouble()) / Network.size())
        println("Churn Ratio: $churnRatio")

//      overlay connection stability
        val allSubStreams = Network::class.activeNodes()
            .map { isConnected((it.getProtocol(flashcrowdid) as FlashcrowdProtocol).substreams) }
//      val allSubStreams = (0 until Network.size()).mapNotNull { Network.get(it)?.getProtocol(pid) }.map{ isConnected((it as FlashcrowdProtocol).substreams) }
        val connStability = allSubStreams.sumBy { it.first }
        val overStability = allSubStreams.sumBy { it.second }
        println("Fraction of Connected Peers: " + (connStability.toDouble() / allSubStreams.size))
        println("Fraction of Stable Peers: " + (overStability.toDouble() / allSubStreams.size))

//      bandwidth utilisation

        println("\n--- End of Statistics ---\n")
        return false
    }

    companion object {
        private const val PAR_FLASHCROWD = "flashcrowd"
        private const val PAR_SWARMING = "swarm"
        private const val PAR_DHT = "dht"
    }
}