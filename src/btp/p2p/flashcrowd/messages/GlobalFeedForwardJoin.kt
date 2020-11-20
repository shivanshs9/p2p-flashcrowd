package btp.p2p.flashcrowd.messages

import peersim.kademlia.events.ProtocolOperation
import java.math.BigInteger

/**
 * Created by shivanshs9 on 20/11/20.
 */
class GlobalFeedForwardJoin(protocolPid: Int, nodeId: BigInteger, stream: Int) :
    ProtocolOperation<Int>(protocolPid, nodeId, stream)