package btp.p2p.flashcrowd.messages

import peersim.dht.message.DHTMessage
import peersim.dht.utils.Address

sealed class BaseMessage<T>(address: Address, refMessageId: Long, val data: T) : DHTMessage(address, refMessageId) {
    override fun onDelivered(pid: Int): DHTMessage? {
        println("delivered: $pid")
        return null
    }

    override fun onFailure(pid: Int): DHTMessage? {
        println("failed: $pid")
        return null
    }
}

class StoreMessage<T>(address: Address, data: T): BaseMessage<T>(address, 0, data)
