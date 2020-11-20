package btp.p2p.flashcrowd.messages

import peersim.kademlia.events.ProtocolOperation
import java.math.BigInteger

/**
 * Created by shivanshs9 on 20/11/20.
 */
class NetworkJoin(protocolPid: Int, nodeId: BigInteger, rank: Int) : ProtocolOperation<Int>(protocolPid, nodeId, rank)