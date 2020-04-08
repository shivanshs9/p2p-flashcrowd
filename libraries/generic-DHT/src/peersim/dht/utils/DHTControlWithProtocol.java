package peersim.dht.utils;

import peersim.config.Configuration;

/**
 * Base class for all DHT protocol controls that have a protocol parameter for the {@link peersim.dht.DHTProtocol}.
 */
public abstract class DHTControlWithProtocol extends DHTControl {

    /**
     * The {@value #PAR_DHT_PROTOCOL} configuration parameter which references the DHTProtocol object.
     *
     * @config
     */
    protected static final String PAR_DHT_PROTOCOL = "protocol";

    /**
     * Stores the protocol identifier.
     */
    protected final int pid;

    /**
     * Standard constructor.
     *
     * @param prefix Configuration file prefix.
     */
    public DHTControlWithProtocol(String prefix) {
        super(prefix);
        this.pid = Configuration.getPid(prefix + "." + PAR_DHT_PROTOCOL);
    }


}
