package peersim.dht.adversary;

import org.junit.BeforeClass;
import org.junit.Test;
import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dht.DHTProtocol;
import peersim.dht.base.Loader;

import static org.junit.Assert.*;

/**
 * Created by baumeist on 8/11/17.
 */
public class AdversaryManagerPercentTest extends Loader {

    @BeforeClass
    public static void setup() throws Exception {
        initSimulator(Loader.BASE_PATH + "adversary_percent.cfg");
        assertTrue(Network.size() == 14);
    }

    @Test
    public void execute() throws Exception {
        int dhtId = Configuration.lookupPid("generic_dht");

        int adversaryCount = 0;
        for(int i =0; i < Network.size(); i++){
            Node n = Network.get(i);
            DHTProtocol dht = (DHTProtocol)n.getProtocol(dhtId);
            if(dht.isAdversary())
                adversaryCount++;
        }

        // should be a total of 14 nodes with 25% adversaries
        assertTrue(adversaryCount == 4);
    }

}