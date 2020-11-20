package btp.p2p.flashcrowd.messages

import peersim.kademlia.events.ProtocolOperation
import java.math.BigInteger

/**
 * Created by shivanshs9 on 20/11/20.
 */
class SterileJoin(protocolPid: Int, nodeId: BigInteger, stream: Int, level: Int) :
    ProtocolOperation<Pair<Int, Int>>(protocolPid, nodeId, stream to level)