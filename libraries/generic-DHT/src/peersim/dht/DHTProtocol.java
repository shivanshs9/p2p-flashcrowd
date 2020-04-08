package peersim.dht;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.dht.routingtable.DHTRoutingTable;
import peersim.dht.routingtable.NHopRoutingTable;
import peersim.dht.router.DHTRouter;
import peersim.dht.router.DHTRouterGreedy;
import peersim.dht.utils.Address;
import peersim.edsim.EDProtocol;

public class DHTProtocol implements EDProtocol, Cloneable {

    /**
     * The {@value #PAR_LINK} configuration parameter defines which protocol
     * is used to store the links (ie the peer relationships).
     *
     * @config
     */
    private static final String PAR_LINK = "topology";

    /**
     * The {@value #PAR_TRANS} configuration parameter defines which protocol
     * is used to transport the messages (ie the connections between peers).
     *
     * @config
     */
    private static final String PAR_TRANS = "transport";

    /**
     * The {@value #PAR_ROUTER} configuration parameter defines which routing protocol
     * the simulator should use: defaults to the {@link peersim.dht.router.DHTRouterGreedy}
     * class.
     *
     * @config
     */
    private static final String PAR_ROUTER = "router";

    /**
     * The {@value #PAR_ROUTING_TABLE} configuration parameter defines which routing table protocol
     * the simulator should use: defaults to the {@link NHopRoutingTable}
     * class.
     *
     * @config
     */
    private static final String PAR_ROUTING_TABLE = "routing_table";

    private final String prefix;
    private final int myPid, linkPid, transportPid;
    private DHTRouter router = null;
    private DHTRoutingTable routingTable = null;

    private Address address = null;

    private boolean isAdversary = false;

    public DHTProtocol(String prefix) {
        this.prefix = prefix;
        this.linkPid = Configuration.getPid(this.prefix + "." + PAR_LINK);
        this.transportPid = Configuration.getPid(this.prefix + "." + PAR_TRANS);
        this.address = new Address(CommonState.r.nextDouble());

        // the the protocol id of this protocol
        String[] name = this.prefix.split("\\.");
        this.myPid = Configuration.lookupPid(name[name.length-1]);
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        this.getRouter(node).route(this.getRoutingTable(node), node, pid, this.transportPid, this.linkPid, event);
    }

    /**
     * Set the address of a node in the DHT.
     *
     * @param address value between 0 and 1
     */
    public void setAddress(double address) {

        this.address = new Address(address);
    }

    /**
     * @return address of the node
     */
    public Address getAddress() {
        return this.address;
    }


    public DHTRouter getRouter(Node node){
        if (this.router != null)
            return this.router;
        try {
            // Load the configured routing protocol
            if(Configuration.contains(this.prefix + "." + PAR_ROUTER))
                this.router = (DHTRouter)node.getProtocol(Configuration.getPid(prefix + "." + PAR_ROUTER));
            else
                this.router = new DHTRouterGreedy("");
        } catch (Exception e) {
            System.err.println(String.format("Error loading a routing protocol: %s", e.getMessage()));
            System.exit(5); // abort
        }
        return this.router;
    }

    public DHTRoutingTable getRoutingTable(Node node){
        if (this.routingTable != null)
            return this.routingTable;
        try {
            // Load the configured routing protocol
            if(Configuration.contains(this.prefix + "." + PAR_ROUTING_TABLE))
                this.routingTable = (DHTRoutingTable) node.getProtocol(Configuration.getPid(prefix + "." + PAR_ROUTING_TABLE));
            else
                this.routingTable = new NHopRoutingTable("");
        } catch (Exception e) {
            System.err.println(String.format("Error loading a routingtable protocol: %s", e.getMessage()));
            System.exit(5); // abort
        }
        return this.routingTable;
    }

    /**
     * Set a node to be an adversary.
     * @param isAdversary True = node is adversary; False = node is not an adversary.
     */
    public void setAdversary(boolean isAdversary){ this.isAdversary = isAdversary; }

    /**
     * Get status is node is an adversary.
     * @return True = node is adversary; False = node is not an adversary.
     */
    public boolean isAdversary(){ return this.isAdversary; }

    /**
     * Replicate this object by returning an identical copy.<br>
     * It is called by the initializer and do not fill any particular field.
     *
     * @return Object
     */
    public Object clone() {
        DHTProtocol dolly = new DHTProtocol(this.prefix);
        return dolly;
    }

    @Override
    public String toString() {
        if(!this.isAdversary())
            return String.format("DHT Location: %s", this.getAddress());
        return String.format("DHT Location: %s (adversary)", this.getAddress());
    }
}
