package btp.p2p.flashcrowd

import peersim.core.Network
import peersim.core.Node
import kotlin.reflect.KClass

/**
 * Created by shivanshs9 on 21/11/20.
 */
fun KClass<Network>.activeNodes(): Collection<Node> =
    (0 until Network.size()).map { Network.get(it) }.filter { it.isUp }

/**
 * Returns the sum of all values produced by [selector] function applied to each element in the collection.
 */
inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum: Long = 0
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

fun Boolean.toInt() = if (this) 1 else 0