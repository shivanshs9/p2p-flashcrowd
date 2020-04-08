package peersim.dht.loopDetection;

import peersim.core.Node;
import peersim.dht.message.DHTMessage;

public abstract class LoopDetection {
	protected final String prefix;

	public LoopDetection(String prefix){
		this.prefix = prefix;
	}

	public abstract boolean checkVisitedNode(Node node, DHTMessage message);
}
