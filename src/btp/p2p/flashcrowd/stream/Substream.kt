package btp.p2p.flashcrowd.stream

import btp.p2p.flashcrowd.messages.StreamNodeData

/**
 * Created by shivanshs9 on 20/11/20.
 */
class Substream(val streamId: Int, var isFertile: Boolean) {
    var level: Int = 0
    val children: MutableList<StreamNodeData> = mutableListOf()
}