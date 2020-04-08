package peersim.dht.message;

import peersim.dht.DHTProtocol;
import peersim.dht.utils.Address;

/**
 * Sends a 
 * @author todd
 *
 */
public class PingOnlyMessage extends DHTMessage {


	public PingOnlyMessage(Address targetAddress) {
		super(targetAddress);
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

