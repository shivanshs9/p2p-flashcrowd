package peersim.dht.message;

import peersim.dht.DHTProtocol;
import peersim.dht.utils.Address;

/**
 * Sends a 
 * @author todd
 *
 */
public class PingMessage extends DHTMessage {


	public PingMessage(Address targetAddress) {
		super(targetAddress);
	}

	@Override
	public DHTMessage onDelivered(int pid) {
		DHTPath path = this.getConnectionPath();
		DHTProtocol targetProtocol = (DHTProtocol) path.getSource().node.getProtocol(pid);

		return new PongMessage(this, targetProtocol.getAddress());
	}

	@Override
	public DHTMessage onFailure(int pid) {
		return null;
	}
}

