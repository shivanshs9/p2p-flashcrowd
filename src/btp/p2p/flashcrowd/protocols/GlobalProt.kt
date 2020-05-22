package btp.p2p.flashcrowd.protocols

import peersim.config.Configuration
import peersim.core.Node
import peersim.edsim.EDProtocol
import peersim.kademlia.events.ProtocolOperation
import peersim.kademlia.KademliaProtocol
import peersim.kademlia.rpc.ResultFindValueOperation

class GlobalProt(val prefix: String) : EDProtocol {
    private val pid: Int = Configuration.getPid("$prefix.$PAR_DHT")

    override fun clone(): Any {
        return GlobalProt(prefix)
    }

    override fun processEvent(p0: Node?, p1: Int, p2: Any?) {
    }

    fun add(node: Int, level: Int){

        val tmp = nodeMap[level]
        tmp?.add(node)
        if (tmp != null) {
            nodeMap[level] = tmp
        }
        else{
            nodeMap[level] = mutableListOf(node)
        }
    }

    fun remove(node: Int, level: Int){

        val tmp = nodeMap[level]
        tmp?.remove(node)
        if(tmp != null)  nodeMap[level] = tmp
    }

    fun getList(level: Int): MutableList<Int>? {
        return nodeMap[level]
    }

    fun getNode(nodeId: Int):Node{

        return networkMap[nodeId]!!
    }

    fun setNode(node: Node){

        networkMap[node.getID().toInt()] = node
    }

    fun setJoin(node: Int){

        joinedMap[node] = true
    }

    fun hasJoined(node: Int):Boolean{

        if(joinedMap[node] == true) return true
        return false
    }

    companion object {
        private const val PAR_DHT = "dht"
        val nodeMap: MutableMap<Int, MutableList<Int>> = mutableMapOf<Int, MutableList<Int>>()
        val networkMap: MutableMap<Int, Node> = mutableMapOf<Int, Node>()
        val joinedMap: MutableMap<Int, Boolean> = mutableMapOf<Int, Boolean>()


    }
}