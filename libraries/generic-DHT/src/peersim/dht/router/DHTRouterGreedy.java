package peersim.dht.router;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.dht.DHTProtocol;
import peersim.dht.observer.GlobalStatsObserver;
import peersim.dht.routingtable.DHTRoutingTable;
import peersim.dht.message.DHTMessage;
import peersim.dht.utils.Address;
import peersim.transport.Transport;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DHTRouterGreedy extends DHTRouter {

    /**
     * The {@value #PAR_RANDOMNESS} configuration parameter defines the
     * amount of randomness to introduce into the greedy routing algorithm:
     * defaults to the value 0.0. The value range is 0.0 - 1.0. 0 is no
     * randomness and 1 is completely random.
     *
     * @config
     */
    private static final String PAR_RANDOMNESS = "randomness";

    private final double randomness;

    public DHTRouterGreedy(String prefix) {
        super(prefix);
        this.randomness = Configuration.getDouble(prefix + "." + PAR_RANDOMNESS, 0.0);
    }

    protected void sendNext(Node node, int pid, int transportPid, int linkPid, DHTMessage message, Node next,
                          RoutingState state){
        Transport transport = (Transport) node.getProtocol(transportPid);

        if(next == null){
            // ran out of peers to successfully route the message
            this.sendBacktrack(node, pid, transportPid, linkPid, message, state.getPrevious());
        } else {
            state.addRoutedTo(next);
            GlobalStatsObserver.addRoutingChoice(state.getRoutedTo().size());
            transport.send(node, next, message, pid);
        }
    }

    @Override
    protected void routeNextNode(DHTRoutingTable routingTable, Node node, int pid, int transportPid, int linkPid, DHTMessage message){
        //  Is this the first time this node has handled this message/
        if(!message.hasRoutingState(node)) {
            // Should we do random routing here?
            boolean routeRandomly = CommonState.r.nextDouble() <= this.randomness;
            // create and save a routing state
            message.saveRoutingState(node, new RoutingState(message.getPreviousNode(), routeRandomly));
        }

        RoutingState state = (RoutingState)message.getRoutingState(node);

        // Two routing modes, random and greedy
        Node next = null;
        if(state.isRandomRouting()) {
            next = this.getRandomNode(routingTable, node, linkPid, state.getPrevious(), state.getRoutedTo());
        } else {
            next = this.getNextGreedyNode(routingTable, node, linkPid, pid, message.getTarget(), state.getPrevious(),
                    state.getRoutedTo());
        }
        this.sendNext(node, pid, transportPid, linkPid, message, next, state);
    }

    public Object clone() {
        DHTRouterGreedy dolly = new DHTRouterGreedy(this.prefix);
        return dolly;
    }

    protected Node getRandomNode(DHTRoutingTable routingTable, Node node, int linkPid, Node previous, Collection<Node> alreadyTried)
    {
        List<DHTRoutingTable.RoutingTableEntry> lookups = routingTable.getRoutingTableEntries(node, linkPid);

        // randomly pick an entry from them routingtable table
        while(lookups.size() > 0){
            int randomIndex = CommonState.r.nextInt(lookups.size());
            DHTRoutingTable.RoutingTableEntry entry = lookups.get(randomIndex);
            lookups.remove(randomIndex);

            // check if we have already routed to the given entry
            if (entry.routeToNode.equals(node))
                continue;
            if(entry.routeToNode.equals(previous))
                continue;
            if(alreadyTried != null && alreadyTried.contains(entry.routeToNode))
                continue;
            return entry.routeToNode; // found one
        }
        return null;
    }

    protected Node getNextGreedyNode(DHTRoutingTable routingTable, Node node, int linkPid, int pid, Address target, Node previous,
                                   Collection<Node> alreadyTried){
        Node next = null;  // stores next node to route to
        Address nextTargetAddress = null; // stores the target address used to compare entries
        int nextHops = 0; // store hop distance to next node, prefer shorter hops
        List<DHTRoutingTable.RoutingTableEntry> lookups = routingTable.getRoutingTableEntries(node, linkPid);

        for (DHTRoutingTable.RoutingTableEntry entry : lookups) {
            // check if we have already routed to the given entry
            if (entry.routeToNode.equals(node))
                continue;
            if(entry.routeToNode.equals(previous))
                continue;
            if(alreadyTried != null && alreadyTried.contains(entry.routeToNode))
                continue;

            // check the distance between the entry and our target
            Address nAddress = ((DHTProtocol) entry.targetNode.getProtocol(pid)).getAddress();
            // entry is closer then the closest option ATM
            // if distance is the same pick the closer node
            if (next == null ||
                    nAddress.distance(target) < nextTargetAddress.distance(target) ||
                    (nAddress.distance(target) == nextTargetAddress.distance(target) && entry.hopDistance < nextHops)) {
                next = entry.routeToNode;
                nextTargetAddress = nAddress;
                nextHops = entry.hopDistance;
            }
        }
        return next;
    }

    protected class RoutingState{
        private final Node previous;
        private LinkedList<Node> routedTo = new LinkedList<>();
        private boolean isRandom = false;

        public RoutingState(Node previous, boolean randomRouting){
            this.previous = previous;
            this.isRandom = randomRouting;
        }

        public Node getPrevious(){ return this.previous; }

        public boolean isRandomRouting(){ return this.isRandom; }

        public void addRoutedTo(Node n){
            this.routedTo.add(n);
        }

        public Collection<Node> getRoutedTo(){ return this.routedTo; }
    }
}
