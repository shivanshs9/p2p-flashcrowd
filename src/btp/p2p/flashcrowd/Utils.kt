package btp.p2p.flashcrowd

import peersim.kademlia.toAscii
import kotlin.math.floor
import kotlin.math.log10

object MsgTypes {
    val STORE_NUM_LEVELS = "num_levels".toAscii()

    val FIND_ANCESTORS = "ancestors".toAscii()

    val CONN_FERTILE_TREE = "fertile".toAscii()
    val CONN_STERILE_TREE = "sterile".toAscii()

    const val FIND_VAL = 1
    const val STORE_VAL = 2
    const val FIND_VAL_STERILE = 3
    const val STORE_VAL_STERILE = 4
    const val CONN_REQ = 5
    const val CONN_SUCCESS = 6
    const val CONN_FAILURE = 7
    const val CONN_REQ_STERILE = 8
    const val CONN_SUCCESS_STERILE = 9
}

object Constants {
    const val KEY_PARENT_LIST = "parent__stream.%d_level.%d"
    const val KEY_GLOBAL_LIST = "global__stream.%d"
    const val KEY_NUM_LEVELS = "num_of_levels"

    const val fetchTimeout: Long = 2000
    const val globalTimeout: Long = 4000
    const val globalDelay: Long = 500
    const val SterileDelay: Long = 7500
}

object Utils {
    fun getLevel(rank: Int): Int {
        val base = Simulator.bandwidthK1
        val ans: Int = floor(log10((base - 1) * rank.toDouble() + 1) / (log10(base.toDouble()) + 0.001)).toInt()
        return ans + 1
    }
}