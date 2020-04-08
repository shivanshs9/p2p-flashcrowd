package peersim.dht.loopDetection;

import peersim.core.Node;
import peersim.core.Protocol;
import peersim.dht.message.DHTMessage;
import peersim.dht.message.DHTPath;
import peersim.dht.message.PathEntry;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class NoLoopDetection extends LoopDetection implements Protocol{

	public NoLoopDetection(String prefix) {
		super(prefix);
	}
	
	/**
	 * Replicate this object by returning an identical copy.<br>
	 * It is called by the initializer and do not fill any particular field.
	 * 
	 * @return Object
	 */
	public Object clone() {
		NoLoopDetection dolly = new NoLoopDetection(this.prefix);
		return dolly;
	}

	@Override
	public boolean checkVisitedNode(Node node, DHTMessage message) {
		return false;
	}
}
