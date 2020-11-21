package btp.p2p.flashcrowd.stream

import btp.p2p.flashcrowd.messages.StreamNodeData

/**
 * Created by shivanshs9 on 20/11/20.
 */
open class Substream(val streamId: Int, var isFertile: Boolean) {
    var level: Int = 0
    val children: MutableList<StreamNodeData> = mutableListOf()
    val parents: MutableList<StreamNodeData> = mutableListOf()
}

class MeshSubstream(stream: Substream) : Substream(stream.streamId, stream.isFertile) {
    val potentialParents: MutableList<StreamNodeData> = mutableListOf()
    var countPendingFetch: Int = 0

    init {
        level = stream.level
    }
}
