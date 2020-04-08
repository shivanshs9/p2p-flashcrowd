package peersim.dht.routingtable;

import org.junit.BeforeClass;
import org.junit.Test;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dht.base.Loader;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by baumeist on 5/14/17.
 */
public class NHopRoutingTableThreeHopTest extends Loader {

    @BeforeClass
    public static void setup() throws Exception {
        initSimulator(Loader.BASE_PATH + "routing_table_3.cfg");
        assertTrue(CommonState.r.getLastSeed() == 1234567890);
        assertTrue(Network.size() == 14);
    }

    @Test
    public void getRoutingTableEntries() throws Exception {
        int routingTableId = Configuration.getPid("protocol.generic_dht.routing_table");
        int topologyId = Configuration.getPid("protocol.generic_dht.topology");

        // check the first node
        Node n = Network.get(0);
        NHopRoutingTable rt = (NHopRoutingTable)n.getProtocol(routingTableId);
        List<DHTRoutingTable.RoutingTableEntry> entries = rt.getRoutingTableEntries(n, topologyId);
        int[] node0peers = new int[]{2,10,11,13};
        int[] node0peersAll = new int[]{2,10,11,13,1,3,4,5,6,8,12,7,9};
        for(DHTRoutingTable.RoutingTableEntry e : entries) {
            assertTrue(this.contains(node0peers, e.routeToNode.getIndex()));
            assertTrue(this.contains(node0peersAll, e.targetNode.getIndex()));
        }
    }
}