package btp.p2p.flashcrowd

import peersim.kademlia.events.RPC
import peersim.kademlia.events.RPCPrimitive
import peersim.kademlia.events.RPCResultPrimitive
import java.math.BigInteger

class MsgTypes{

    companion object{

        const val FIND_VAL = 1
        const val STORE_VAL = 2
        const val CONN_REQ = 5
        const val CONN_SUCCESS = 6
        const val CONN_FAILURE = 7
    }
}

class ConnectionRequest(srcNodeId: BigInteger, destNodeId: BigInteger, data: Int?, type: Int) :
    RPCPrimitive<Int>(srcNodeId, destNodeId, data, type) {

}

class ConnectionRequestResponse(msg: RPCPrimitive<*>, data: Int?, status: Int) :
    RPCResultPrimitive<Int>(msg, data, status){

}