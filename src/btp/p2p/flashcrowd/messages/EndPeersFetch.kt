package btp.p2p.flashcrowd.messages

import peersim.kademlia.events.SimpleEvent
import peersim.kademlia.toAscii

/**
 * Created by shivanshs9 on 21/11/20.
 */
class EndPeersFetch(val streamId: Int) : SimpleEvent("swarming".toAscii())