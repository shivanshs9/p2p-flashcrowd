package peersim.dht.traffic;

import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dht.DHTProtocol;
import peersim.dht.message.PingOnlyMessage;
import peersim.dht.utils.DHTControlWithProtocol;
import peersim.edsim.EDSimulator;

/**
 * Generates one way traffic between random nodes.
 * 
 * @author todd
 * 
 */
public class RandomPingTraffic extends DHTControlWithProtocol {

	/**
	 * Generate messages that route from a random node A to a random node B
	 * 
	 * @param prefix
	 */
	public RandomPingTraffic(String prefix) {
		super(prefix);
	}

	public boolean execute() {
		int size = Network.size();
		Node sender, target;
		DHTProtocol senderDht;
		do {
			sender = Network.get(CommonState.r.nextInt(size));
			senderDht = (DHTProtocol)sender.getProtocol(this.pid);
			target = Network.get(CommonState.r.nextInt(size));
		} while (sender == null || sender.isUp() == false || target == null
				|| target.isUp() == false || sender.equals(target) || senderDht.isAdversary());
		DHTProtocol targetProtocol = (DHTProtocol) target.getProtocol(pid);
		PingOnlyMessage message = new PingOnlyMessage(targetProtocol.getAddress());
		EDSimulator.add(10, message, sender, pid);
		return false;
	}
}