package btp.p2p.flashcrowd

import peersim.Simulator
import peersim.config.Configuration

object Simulator {
    private const val PREFIX = "btp"
    private const val PAR_SYSTEM = "system"
    private const val PAR_STREAM = "stream"
    private const val PAR_SUBSTREAMS = "substreams"
    private const val PAR_PEER = "peer"
    private const val PAR_PEER_CAPACITY = "capacity"

    val noOfSubStreamTrees by lazy {
        Configuration.getInt("$PREFIX.$PAR_STREAM.$PAR_SUBSTREAMS", 1)
    }

    val bandwidthK1 by lazy {
        Configuration.getInt("$PREFIX.$PAR_PEER.$PAR_PEER_CAPACITY.outgoing", 1)
    }

    val bandwidthK2 by lazy {
        Configuration.getInt("$PREFIX.$PAR_PEER.$PAR_PEER_CAPACITY.total", 2)
    }

    val systemCapacity by lazy {
        Configuration.getInt("$PREFIX.$PAR_SYSTEM.$PAR_PEER_CAPACITY", 5)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        Simulator.main(args)
    }
}