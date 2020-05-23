package btp.p2p.flashcrowd

import peersim.kademlia.events.RPC
import peersim.kademlia.events.RPCPrimitive
import peersim.kademlia.events.RPCResultPrimitive
import java.math.BigInteger

class MsgTypes{

    companion object{

        const val FIND_VAL = 1
        const val STORE_VAL = 2
        const val FIND_VAL_STERILE = 1
        const val STORE_VAL_STERILE = 2
        const val CONN_REQ = 5
        const val CONN_SUCCESS = 6
        const val CONN_FAILURE = 7
        const val CONN_REQ_STERILE = 8
        const val CONN_SUCCESS_STERILE = 6
    }
}

class Constants{

    companion object{

        const val K = 4
        const val globalTimeout:Long = 3000
        const val globalDelay:Long = 500
        const val SterileDelay:Long = 7500

    }
}

class ConnectionRequest(srcNodeId: BigInteger, destNodeId: BigInteger, data: Int?, type: Int) :
    RPCPrimitive<Int>(srcNodeId, destNodeId, data, type) {

}