package btp.p2p.flashcrowd.messages

import btp.p2p.flashcrowd.MsgTypes
import peersim.kademlia.events.RPCPrimitive
import peersim.kademlia.events.RPCResultPrimitive
import java.math.BigInteger

/**
 * Created by shivanshs9 on 19/11/20.
 */

class ConnectionRequest(
    srcNodeId: BigInteger,
    destNodeId: BigInteger,
    streamNodeData: StreamNodeData,
    isFertile: Boolean
) :
    RPCPrimitive<StreamNodeData>(srcNodeId, destNodeId, streamNodeData) {
    init {
        type = if (isFertile) MsgTypes.CONN_FERTILE_TREE else MsgTypes.CONN_STERILE_TREE
    }
}

class ConnectionResult(requestOp: ConnectionRequest, streamId: Int, status: Int = RPCResultPrimitive.STATUS_SUCCESS) :
    RPCResultPrimitive<Int>(requestOp, streamId, status)