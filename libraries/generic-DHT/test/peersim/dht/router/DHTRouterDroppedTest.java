package peersim.dht.router;

import org.junit.Test;
import peersim.dht.base.Loader;

import static org.junit.Assert.*;

/**
 * Created by baumeist on 5/21/17.
 */
public class DHTRouterDroppedTest extends Loader {

    @Test
    public void routerDropped() throws Exception{
        initSimulator(Loader.BASE_PATH + "message_dropped.cfg");
    }
}