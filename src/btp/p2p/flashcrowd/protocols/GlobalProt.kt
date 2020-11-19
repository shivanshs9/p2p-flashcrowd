package btp.p2p.flashcrowd.protocols

import peersim.core.Node

object GlobalProt {
    fun add(node: Int, level: Int) {
        val tmp = nodeMap[level]
        tmp?.add(node)
        if (tmp != null) {
            nodeMap[level] = tmp
        } else {
            nodeMap[level] = mutableListOf(node)
        }
    }

    fun addSterile(node: Int, level: Int) {
        val tmp = sterileNodeMap[level]
        tmp?.add(node)
        if (tmp != null) {
            sterileNodeMap[level] = tmp
        } else {
            sterileNodeMap[level] = mutableListOf(node)
        }
    }

    fun getSterileList(level: Int): MutableList<Int>? {
        return sterileNodeMap[level]
    }

    fun remove(node: Int, level: Int) {
        val tmp = nodeMap[level]
        tmp?.remove(node)
        if (tmp != null) nodeMap[level] = tmp
    }

    fun getList(level: Int): MutableList<Int>? {
        return nodeMap[level]
    }

    fun getNode(nodeId: Int): Node {
        return networkMap[nodeId]!!
    }

    fun setNode(node: Node) {
        networkMap[node.id.toInt()] = node
    }

    fun setJoin(node: Int) {
        joinedMap[node] = true
    }

    fun hasJoined(node: Int): Boolean {
        if (joinedMap[node] == true) return true
        return false
    }

    fun getgloballist(): MutableList<Int> {
        return globalfeed
    }

    fun globaladd(node: Int) {
        globalfeed.add(node)
    }

    fun setSterileJoin(node: Int) {
        joinedSterileMap[node] = true
    }

    fun hasJoinedSterile(node: Int): Boolean {
        if (joinedSterileMap[node] == true) return true
        return false
    }

    fun disconnectSterile(node: Int) {
        joinedSterileMap[node] = false
    }

    val nodeMap: MutableMap<Int, MutableList<Int>> = mutableMapOf()
    val sterileNodeMap: MutableMap<Int, MutableList<Int>> = mutableMapOf()
    val networkMap: MutableMap<Int, Node> = mutableMapOf()
    val joinedMap: MutableMap<Int, Boolean> = mutableMapOf()
    val globalfeed: MutableList<Int> = mutableListOf()
    val joinedSterileMap: MutableMap<Int, Boolean> = mutableMapOf()
}