package btp.p2p.flashcrowd

import peersim.core.Network
import peersim.core.Node
import kotlin.reflect.KClass

/**
 * Created by shivanshs9 on 21/11/20.
 */
fun KClass<Network>.activeNodes(): Collection<Node> =
    (0 until Network.size()).map { Network.get(it) }.filter { it.isUp }