package peersim.dht.routingtable;

import peersim.core.Linkable;
import peersim.core.Node;
import peersim.core.Protocol;

import java.util.List;


/**
 * Models the available routing table entries that a node will use
 * to make routing decisions. This represents a node's view of the topology.
 */
public abstract class DHTRoutingTable implements Protocol {


    /**
     * Configuration prefix.
     */
    protected final String prefix;

    /**
     * @param prefix configuration prefix.
     */
    public DHTRoutingTable(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @param topology        The linkable object used to represent the topology.
     * @param neighborToCheck Check if this node is directly peered.
     * @return True if node is a direct peer; otherwise False.
     */
    public boolean isDirectNeighbor(Linkable topology, Node neighborToCheck) {
        for (int i = 0; i < topology.degree(); i++) {
            Node n = topology.getNeighbor(i);
            if (n.equals(neighborToCheck))
                return true;
        }
        return false;
    }

    /**
     * Get routing table entries.
     *
     * @param currentNode Get routing table entries for this node.
     * @param topologyPid The protocol ID of the topology protocol.
     * @return List of routing table entries.
     */
    public abstract List<RoutingTableEntry> getRoutingTableEntries(Node currentNode, int topologyPid);


    /**
     * @return Creates a new duplicate object.
     */
    public abstract Object clone();


    /**
     * Represents a routing table entry.
     */
    public class RoutingTableEntry {


        /**
         * The direct peer that can be routed to.
         */
        public final Node routeToNode;

        /**
         * The node that can be used for routing decisions. Note: the protocol will route through the
         * {@link #routeToNode} Node in order to reach this node.
         */
        public final Node targetNode;

        /**
         * The distance between the source node and the {@link #targetNode}.
         */
        public final int hopDistance;

        /**
         * @param routeTo     {@link #routeToNode}
         * @param targetNode  {@link #targetNode}
         * @param hopDistance {@link #hopDistance}
         */
        public RoutingTableEntry(Node routeTo, Node targetNode, int hopDistance) {
            this.routeToNode = routeTo;
            this.targetNode = targetNode;
            this.hopDistance = hopDistance;
        }

        /**
         * @return String representing a routing table entry. Format: (Hop distance) Route to Node ID -> Target Node ID.
         */
        @Override
        public String toString() {
            return String.format("(%d) %d -> %d", this.hopDistance, this.routeToNode.getID(), this.targetNode.getID());
        }
    }
}
