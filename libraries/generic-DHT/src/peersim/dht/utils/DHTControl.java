package peersim.dht.utils;

import peersim.core.Control;

/**
 * Base class for all DHT protocol controls.
 */
public abstract class DHTControl implements Control {

    /**
     * Stores the control's name.
     */
    protected final String prefix;

    /**
     * Standard constructor.
     *
     * @param prefix Configuration file prefix.
     */
    public DHTControl(String prefix) {
        this.prefix = prefix;
    }
}
