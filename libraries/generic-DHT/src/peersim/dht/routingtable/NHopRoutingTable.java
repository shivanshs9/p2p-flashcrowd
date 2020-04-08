package peersim.dht.routingtable;

import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Node;

import java.util.LinkedList;
import java.util.List;


/**
 * Represents a routing table that can look up to N hops away from the source node to make routing decisions.
 */
public class NHopRoutingTable extends DHTRoutingTable {

    /**
     * The {@value #PAR_HOPS} configuration parameter defines the
     * number of hops the routing table table will use for routing decisions.
     * Defaults to 2 hops.
     *
     * @config
     */
    private static final String PAR_HOPS = "hops";

    /**
     * The look ahead hop count value. 1 is equal to only looking at direct peers. Value must be greater that 0.
     */
    private final int hopCount;

    /**
     * @param prefix Configuration prefix
     */
    public NHopRoutingTable(String prefix) {
        super(prefix);
        this.hopCount = Configuration.getInt(prefix + "." + PAR_HOPS, 2);
        if (this.hopCount < 1) {
            System.err.println("Must have a hop count of at least one for the N Lookup Table");
            System.exit(5); // abort
        }
    }

    /**
     * @param currentNode Get routing table entries for this node.
     * @param topologyPid The protocol ID of the topology protocol.
     * @return Routing paths to all nodes within N hops of the current Node.
     */
    public List<RoutingTableEntry> getRoutingTableEntries(Node currentNode, int topologyPid) {
        List<RoutingTableEntry> entries = new LinkedList<>();
        this.addRoutingEntries(entries, currentNode, topologyPid, currentNode, null, 1, this.hopCount);
        return entries;
    }

    /**
     * @return a duplicate copy of this object.
     */
    public Object clone() {
        return new NHopRoutingTable(this.prefix);
    }

    /**
     * @param entries List of entries to add new entries to.
     * @param currentNode The current node we are looking at.
     * @param topologyPid The protocol ID for the topology.
     * @param sourceNode The original node that the routing table is being built for.
     * @param routeToNode The direct peer of the source node that would need to be routed through to reach the target node.
     * @param currentHop How many hops away from the source node.
     * @param stopHop When to stop grabbing routing table entries.
     */
    private void addRoutingEntries(List<RoutingTableEntry> entries, Node currentNode, int topologyPid,
                                   Node sourceNode, Node routeToNode,
                                   int currentHop, final int stopHop) {
        // stop!
        if (currentHop > stopHop)
            return;

        // get the direct neighbor links
        Linkable linkable = (Linkable) currentNode.getProtocol(topologyPid);
        for (int i = 0; i < linkable.degree(); i++) {
            Node n = linkable.getNeighbor(i);

            // don't add your self to the list
            if (n.equals(sourceNode))
                continue;

            Node r = routeToNode;
            if (r == null)
                r = n;
            if (this.addUniqueEntry(entries, r, n, currentHop)) {
                // added a new entry, depth first continue adding entries
                this.addRoutingEntries(entries, n, topologyPid, sourceNode, r, currentHop + 1, stopHop);
            }
        }
    }

    /**
     * @param entries List of routing table entries to add the new entry to.
     * @param routeToNode Direct peer node of the source node that would be routed to.
     * @param targetNode The Node used to make the routing decisions.
     * @param hopDistance Distance between target Node and source Node.
     * @return True if a new entry was added or an existing entry was replaced; otherwise False.
     */
    private boolean addUniqueEntry(List<RoutingTableEntry> entries, Node routeToNode, Node targetNode, int hopDistance) {
        for (int i = 0; i < entries.size(); i++) {
            RoutingTableEntry e = entries.get(i);
            // check if there is already an entry for target node through the same route to node
            if (e.targetNode.equals(targetNode) && e.routeToNode.equals(routeToNode)) {
                // replace entry if new entry is closer
                if (e.hopDistance > hopDistance) {
                    entries.remove(i);
                    entries.add(new RoutingTableEntry(routeToNode, targetNode, hopDistance));
                    return true;
                } else {
                    // Already an equal or better routing entry
                    return false;
                }
            }
        }

        // no equivalent routing entry found, add one
        entries.add(new RoutingTableEntry(routeToNode, targetNode, hopDistance));
        return true;
    }
}
