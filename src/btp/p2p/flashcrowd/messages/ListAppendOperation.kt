package btp.p2p.flashcrowd.messages

import peersim.kademlia.events.ProtocolOperation
import peersim.kademlia.toAscii
import java.math.BigInteger

/**
 * Created by shivanshs9 on 20/11/20.
 */
class ListAppendOperation(protocolPid: Int, nodeId: BigInteger, key: String, data: Any) :
    ProtocolOperation<Pair<String, Any>>(protocolPid, nodeId, key to data) {
    companion object {
        val TYPE_APPEND = "append".toAscii()
    }
}