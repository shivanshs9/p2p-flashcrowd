package btp.p2p.flashcrowd.messages

import peersim.kademlia.events.RPCPrimitive
import peersim.kademlia.events.RPCResultPrimitive
import java.math.BigInteger

/**
 * Created by shivanshs9 on 19/11/20.
 */

class MeshConnectionRequest(
    srcNodeId: BigInteger,
    destNodeId: BigInteger,
    streamNodeData: StreamNodeData
) :
    RPCPrimitive<StreamNodeData>(srcNodeId, destNodeId, streamNodeData)

class MeshConnectionResult(
    requestOp: MeshConnectionRequest,
    streamId: Int,
    status: Int = RPCResultPrimitive.STATUS_SUCCESS
) :
    RPCResultPrimitive<Int>(requestOp, streamId, status)