package btp.p2p.flashcrowd

import peersim.Simulator
import peersim.config.Configuration

object Simulator {
    private const val PREFIX = "btp"
    private const val PAR_STREAM = "stream"
    private const val PAR_SUBSTREAMS = "substreams"

    val noOfSubStreamTrees by lazy {
        Configuration.getInt("$PREFIX.$PAR_STREAM.$PAR_SUBSTREAMS", 1)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        Simulator.main(args)
    }
}