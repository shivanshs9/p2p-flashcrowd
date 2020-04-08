package peersim.dht.topology;

import org.junit.BeforeClass;
import org.junit.Test;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dht.DHTProtocol;
import peersim.dht.base.Loader;
import peersim.dht.routingtable.DHTRoutingTable;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by baumeist on 5/14/17.
 */
public class LoadGMLFailedTest extends Loader {
    @BeforeClass
    public static void setup() throws Exception {
        initSimulator(Loader.BASE_PATH + "load_gml_error_topology.cfg");
    }

    @Test
    public void execute() throws Exception {
        int dhtId = Configuration.lookupPid("generic_dht");
        int topologyId = Configuration.getPid("protocol.generic_dht.topology");

        Node n = Network.get(0);
        DHTProtocol dht = (DHTProtocol)n.getProtocol(dhtId);
        DHTRoutingTable rt = dht.getRoutingTable(n);
        List<DHTRoutingTable.RoutingTableEntry> entries = rt.getRoutingTableEntries(n, topologyId);
    }

}