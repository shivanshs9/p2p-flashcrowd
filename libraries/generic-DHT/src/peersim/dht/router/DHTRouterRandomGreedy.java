package peersim.dht.router;

import peersim.config.Configuration;
import peersim.core.Node;
import peersim.dht.message.DHTMessage;
import peersim.dht.routingtable.DHTRoutingTable;


public class DHTRouterRandomGreedy extends DHTRouterGreedy {

    /**
     * The {@value #PAR_RANDOM_HOPS} configuration parameter defines the
     * number of random hops to take before starting the greedy routing.
     * Defaults to 3.
     *
     * @config
     */
    private static final String PAR_RANDOM_HOPS = "random_hop_count";

    private final double randomHopCount;

    public DHTRouterRandomGreedy(String prefix) {
        super(prefix);
        this.randomHopCount = Configuration.getInt(prefix + "." + PAR_RANDOM_HOPS, 3);
    }

    @Override
    protected void routeNextNode(DHTRoutingTable routingTable, Node node, int pid, int transportPid, int linkPid, DHTMessage message){
        //  Is this the first time this node has handled this message/
        if(!message.hasRoutingState(node)) {
            // create and save a routing state
            message.saveRoutingState(node, new RoutingState(message.getPreviousNode(), false));
        }

        RoutingState state = (RoutingState)message.getRoutingState(node);

        // Two routing modes, random and greedy
        Node next = null;
        if(message.getConnectionPath().size() <= this.randomHopCount) {
            next = this.getRandomNode(routingTable, node, linkPid, state.getPrevious(), state.getRoutedTo());
        } else {
            next = this.getNextGreedyNode(routingTable, node, linkPid, pid, message.getTarget(), state.getPrevious(),
                    state.getRoutedTo());
        }
        this.sendNext(node, pid, transportPid, linkPid, message, next, state);
    }

    public Object clone() {
        DHTRouterRandomGreedy dolly = new DHTRouterRandomGreedy(this.prefix);
        return dolly;
    }
}
