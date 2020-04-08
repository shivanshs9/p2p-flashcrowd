package peersim.dht.message;

import peersim.core.Node;
import peersim.dht.utils.Address;

/**
 * Sends a 
 * @author todd
 *
 */
public class PongMessage extends DHTMessage {

	public PongMessage(PingMessage pingMessage, Address targetAddress) {
		super(targetAddress, pingMessage.getMessageID());
		this.messageStatus = MessageStatus.RETURN_TO_SENDER;

		// save the circuit path back to sender in the routing state of the message
		Node next = null;
		for(PathEntry n : pingMessage.getConnectionPath()){
			if(next != null)
				this.saveRoutingState(n.node, next);
			next = n.node;
		}
	}

	@Override
	public DHTMessage onDelivered(int pid) {
		return null;
	}

	@Override
	public DHTMessage onFailure(int pid) {
		return null;
	}
}

