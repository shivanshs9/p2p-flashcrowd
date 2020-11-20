package btp.p2p.flashcrowd.messages

import btp.p2p.flashcrowd.Constants
import java.math.BigInteger

/**
 * Created by shivanshs9 on 20/11/20.
 */
data class StreamNodeData(val streamId: Int, val nodeId: BigInteger) {
    fun getLevelKey(level: Int) = Constants.KEY_PARENT_LIST.format(streamId, level)
    val globalKey = Constants.KEY_GLOBAL_LIST.format(streamId)
}