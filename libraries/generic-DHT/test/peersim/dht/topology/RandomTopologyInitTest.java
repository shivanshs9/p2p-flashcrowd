package peersim.dht.topology;

import org.junit.Test;
import peersim.dht.base.Loader;

import static org.junit.Assert.*;

/**
 * Created by baumeist on 5/21/17.
 */
public class RandomTopologyInitTest extends Loader {

    @Test
    public void test() throws Exception{
        initSimulator(Loader.BASE_PATH + "random_topology_churn.cfg");
    }
}