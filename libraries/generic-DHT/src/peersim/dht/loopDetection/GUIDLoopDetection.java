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
public class GUIDLoopDetection extends LoopDetection implements Protocol{
	
	public GUIDLoopDetection(String prefix) {
		super(prefix);
	}
	
	/**
	 * Replicate this object by returning an identical copy.<br>
	 * It is called by the initializer and do not fill any particular field.
	 * 
	 * @return Object
	 */
	public Object clone() {
		GUIDLoopDetection dolly = new GUIDLoopDetection(this.prefix);
		return dolly;
	}

	@Override
	public boolean checkVisitedNode(Node node, DHTMessage message) {
		DHTPath path = message.getRoutingPath();
		// ignore the last entry in the routing path
		List<PathEntry> visited = path.size() > 0? path.subList(0, path.size()-1): new LinkedList<>();
		for(PathEntry e : visited)
			if(e.node.equals(node)) return true;
		return false;
	}
}
