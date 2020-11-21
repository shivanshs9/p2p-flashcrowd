package btp.p2p.flashcrowd.controller

import btp.p2p.flashcrowd.activeNodes
import btp.p2p.flashcrowd.messages.SwarmingStart
import peersim.config.Configuration
import peersim.core.Control
import peersim.core.Network
import peersim.edsim.EDSimulator

/**
 * Created by shivanshs9 on 19/11/20.
 */
class DiffusionTrigger(prefix: String) : Control {
    private val protocolId = Configuration.getPid("$prefix.$PAR_PROT")
    private val startDelay = Configuration.getLong("$prefix.$PAR_TIME_DELAY")
    private val startTime: Long = System.currentTimeMillis()

    override fun execute(): Boolean {
        if (System.currentTimeMillis() - startTime < startDelay) return false

        Network::class.activeNodes().forEach { EDSimulator.add(0, SwarmingStart(), it, protocolId) }
        return false
    }

    companion object {
        private const val PAR_PROT = "protocol"

        /**
         * Time delay (in ms) to start swarming
         */
        private const val PAR_TIME_DELAY = "delay_start"
    }
}