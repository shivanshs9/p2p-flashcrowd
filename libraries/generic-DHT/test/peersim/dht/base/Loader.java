package peersim.dht.base;

import org.junit.runner.RunWith;
import peersim.config.Configuration;
import peersim.config.ParsedProperties;
import peersim.dht.base.Quarantine.Quarantine;
import peersim.dht.base.Quarantine.QuarantiningRunner;
import peersim.edsim.EDSimulator;

/**
 * Created by baumeist on 5/14/17.
 */
@RunWith(QuarantiningRunner.class)
@Quarantine({"peersim"})
public abstract class Loader {

    protected final static String BASE_PATH = "test/resources/";

    protected static void initSimulator(String confPath) throws Exception {
        Loader.initConfig(confPath);

        // Run simulator to init variables
        EDSimulator.nextExperiment();
    }

    protected static void initConfig(String confPath){
        Configuration.setConfig(new ParsedProperties(new String[]{confPath}));
    }

    protected boolean contains(final int[] array, final int key) {
        for (final int i : array) {
            if (i == key) {
                return true;
            }
        }
        return false;
    }

}
