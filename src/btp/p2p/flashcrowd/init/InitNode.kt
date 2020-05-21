package btp.p2p.flashcrowd.init

import peersim.core.Node
import peersim.dynamics.NodeInitializer
import peersim.config.Configuration
import kotlin.math.floor
import kotlin.math.log10

class InitNode(prefix: String) : NodeInitializer {

    private fun getlevel(rank: Int):Int{

        val ans: Int = floor(log10((base-1)*rank.toDouble() + 1)/(log10(base.toDouble()) + 0.001)).toInt();
        return ans+1
    }
    override fun initialize(n: Node?) {

        val rank = n?.getID();
        val level = rank?.toInt()?.let { getlevel(it) }
        println(rank.toString() + " " + level.toString())
   }

    companion object{
        var rank = 1
        var base = 4
    }
}