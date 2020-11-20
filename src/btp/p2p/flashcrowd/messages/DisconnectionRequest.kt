package btp.p2p.flashcrowd.messages

import peersim.kademlia.events.RPCPrimitive
import java.math.BigInteger

/**
 * Created by shivanshs9 on 20/11/20.
 */
class DisconnectionRequest(srcNodeId: BigInteger, destNodeId: BigInteger, streamNodeData: StreamNodeData) :
    RPCPrimitive<StreamNodeData>(srcNodeId, destNodeId, streamNodeData)