package btp.p2p.flashcrowd.messages

import peersim.kademlia.events.RPCPrimitive
import java.math.BigInteger

/**
 * Created by shivanshs9 on 19/11/20.
 */

class ConnectionRequest(srcNodeId: BigInteger, destNodeId: BigInteger, data: Int?, type: Int) :
    RPCPrimitive<Int>(srcNodeId, destNodeId, data, type)