package peersim.dht.router;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.core.Protocol;
import peersim.dht.DHTProtocol;
import peersim.dht.routingtable.DHTRoutingTable;
import peersim.dht.loopDetection.GUIDLoopDetection;
import peersim.dht.loopDetection.LoopDetection;
import peersim.dht.message.DHTMessage;
import peersim.dht.observer.store.DHTRoutingDataStore;
import peersim.transport.Transport;

public abstract class DHTRouter implements Protocol {

    /**
     * The {@value #PAR_LOOP} configuration parameter defines which loop
     * detection protocol message sent by the simulator should use:
     * defaults to the {@link peersim.dht.loopDetection.GUIDLoopDetection}
     * class.
     *
     * @config
     */
    protected static final String PAR_LOOP = "loop_detection";

    /**
     * The {@value #PAR_BACK} configuration parameter defines if
     * the routing protocol should back track if it gets stuck
     * in a local minimum:
     * defaults to the value true.
     *
     * @config
     */
    protected static final String PAR_BACK = "can_backtrack";

    /**
     * The {@value #PAR_STORE} configuration parameter defines the file the router will store any routing data.
     * class.
     *
     * @config
     */
    protected static final String PAR_STORE = "route_storage_file";

    /**
     * The {@value #PAR_MAX_ROUTE_LENGTH} configuration parameter defines the maximum routing path length.
     * Defaults to 0 which means its not limited.
     *
     * @config
     */
    protected static final String PAR_MAX_ROUTE_LENGTH = "max_route_length";

    /**
     * The {@value #PAR_DROP_RATE} configuration parameter defines the probability that a given message will be dropped.
     * Defaults to 0.0 (or drop no messages).
     * Normally the {@link peersim.transport.UnreliableTransport} class could be used to model dropped messages;
     * however, {@link peersim.transport.UnreliableTransport} does not notify the protocol that the message was dropped.
     *
     * @config
     */
    protected static final String PAR_DROP_RATE = "drop_rate";


    protected final String prefix;
    protected final Boolean canBackTrack;
    protected final DHTRoutingDataStore dhtFileStore;
    protected final double dropRate;
    protected final int maxRouteLength;

    private LoopDetection loop = null;

    public DHTRouter(String prefix) {
        this.prefix = prefix;
        this.canBackTrack = Configuration.getBoolean(prefix + "." + PAR_BACK, true);
        String dataStoreFileName = Configuration.getString(this.prefix + "." + PAR_STORE, null);
        this.dropRate = Configuration.getDouble(prefix + "." + PAR_DROP_RATE, 0.0);
        this.maxRouteLength = Configuration.getInt(prefix + "." + PAR_MAX_ROUTE_LENGTH, 0);
        DHTRoutingDataStore store = null;
        if (dataStoreFileName != null) {
            try {
                store = DHTRoutingDataStore.getInstance(dataStoreFileName);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        this.dhtFileStore = store;
    }

    /**
     * Write persistent data to the file storage.
     */
    protected void storeRouteData(DHTMessage message) {
        if (this.dhtFileStore != null)
            this.dhtFileStore.storeData(message);
    }

    protected abstract void routeNextNode(DHTRoutingTable routingTable, Node node, int pid, int transportPid, int linkPid, DHTMessage message);

    protected void routeReplyNextNode(DHTRoutingTable routingTable, Node node, int pid, int transportPid, int linkPid, DHTMessage message) {
        Transport transport = (Transport) node.getProtocol(transportPid);
        Linkable linkable = (Linkable) node.getProtocol(linkPid);

        Node next = (Node)message.getRoutingState(node);

        //check the next node can be routed to
        if(!routingTable.isDirectNeighbor(linkable, next)){
            message.setMessageStatus(DHTMessage.MessageStatus.FAILED);
            this.storeRouteData(message);
            return; // can't route back to home, because circuit was broken
        }
        transport.send(node, next, message, pid);
    }

    protected void sendBacktrack(Node node, int pid, int transportPid, int linkPid, DHTMessage message, Node previous) {
        Transport transport = (Transport) node.getProtocol(transportPid);
        if (this.canBackTrack) {
            if (message.prepareToBacktrack()) {
                message.setMessageStatus(DHTMessage.MessageStatus.BACKTRACKED);
                transport.send(node, previous, message, pid);
            } else {
                message.setMessageStatus(DHTMessage.MessageStatus.FAILED);
                this.storeRouteData(message);
            }
        } else {
            message.setMessageStatus(DHTMessage.MessageStatus.FAILED);
            this.storeRouteData(message);
        }
    }

    protected void sendDropped(Node node, int pid, int transportPid, DHTMessage message, Node previous){
        Transport transport = (Transport) node.getProtocol(transportPid);
        if (message.prepareToBacktrack()) {
            message.setMessageStatus(DHTMessage.MessageStatus.DROPPED);
            transport.send(node, previous, message, pid);
        } else {
            message.setMessageStatus(DHTMessage.MessageStatus.FAILED);
            this.storeRouteData(message);
        }
    }

    public void route(DHTRoutingTable routingTable, Node node, int pid, int transportPid, int linkPid, Object event) {
        DHTMessage message = (DHTMessage) event;
        DHTProtocol currentNode = (DHTProtocol) node.getProtocol(pid);

        // Add node to the messages visited list
        message.arrivedAt(node, currentNode);

        // Check if this message was backtracked or dropped to this node
        if (message.getMessageStatus() == DHTMessage.MessageStatus.BACKTRACKED ||
                message.getMessageStatus() == DHTMessage.MessageStatus.DROPPED) {
            message.setMessageStatus(DHTMessage.MessageStatus.FORWARDED);
            routeNextNode(routingTable, node, pid, transportPid, linkPid, message);
            return;
        }

        // Check if the maximum path length has been reached
        if (this.maxRouteLength > 0 && message.getRoutingPath().size() > this.maxRouteLength){
            message.setMessageStatus(DHTMessage.MessageStatus.MAX_LENGTH);
            this.storeRouteData(message);
            return;
        }

        // Drop a message at random, except if it is the first node
        // Message isn't actually dropped. Instead set status to dropped and go back to the previous node
        // This was the easiest way to implemented message dropping.
        if (message.getConnectionPath().size() > 1 && CommonState.r.nextDouble() <= this.dropRate){
            // ok we decided to drop the message
            // check if this was a reply message, in which case fail the delivery because the circuit is broke
            if(message.getMessageStatus() == DHTMessage.MessageStatus.RETURN_TO_SENDER){
                message.setMessageStatus(DHTMessage.MessageStatus.FAILED);
                this.storeRouteData(message);
                return;
            }
            this.sendDropped(node, pid, transportPid, message, message.getPreviousNode());
            return;
        }

        // final destination, no not the movie
        if (currentNode.getAddress().equals(message.getTarget())) {
            message.setMessageStatus(DHTMessage.MessageStatus.DELIVERED);
            this.storeRouteData(message);

            DHTMessage nextMessage = message.onDelivered(pid);
            if (nextMessage != null) // this generates another message?
                route(routingTable, node, pid, transportPid, linkPid, nextMessage); // call routing method
            return; // reached target
        }

        // is this a reply message on a circuit path by default
        if (message.getMessageStatus() == DHTMessage.MessageStatus.RETURN_TO_SENDER) {
            routeReplyNextNode(routingTable, node, pid, transportPid, linkPid, message);
            return;
        }

        // run loop detection
        if (this.getLoopDetection().checkVisitedNode(node, message)) {
            // already visited
            this.sendBacktrack(node, pid, transportPid, linkPid, message, message.getPreviousNode());
            return; // either backtracked or we are done
        }

        message.setMessageStatus(DHTMessage.MessageStatus.FORWARDED);
        routeNextNode(routingTable, node, pid, transportPid, linkPid, message);
    }

    /**
     * Replicate this object by returning an identical copy.<br>
     * It is called by the initializer and do not fill any particular field.
     *
     * @return Object
     */
    public abstract Object clone();

    protected LoopDetection getLoopDetection() {
        if (this.loop != null)
            return this.loop;

        try {
            // Load the configured loop detection protocol
            this.loop = (LoopDetection) Configuration.getInstance(this.prefix + "."
                    + PAR_LOOP, new GUIDLoopDetection(""));

        } catch (Exception e) {
            System.err.println(String.format(
                    "Error loading a loop protocol: %s", e.getMessage()));
            e.printStackTrace();
            System.exit(1); // abort
        }
        return this.loop;
    }
}
