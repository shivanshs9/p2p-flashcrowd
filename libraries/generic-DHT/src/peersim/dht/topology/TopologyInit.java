package peersim.dht.topology;

import peersim.config.Configuration;
import peersim.core.Node;
import peersim.core.OverlayGraph;
import peersim.dynamics.NodeInitializer;
import peersim.graph.Graph;

/**
 * Created by baumeist on 5/21/17.
 */
public abstract class TopologyInit implements NodeInitializer {

    /**
     * The {@value #PAR_LINK} configuration parameter defines which protocol
     * is used to store the links (ie the peer relationships).
     *
     * @config
     */
    private static final String PAR_LINK = "protocol";

    /**
     * Stores the control's name.
     */
    protected final String prefix;
    protected final int linkPid;

    /**
     * Standard constructor.
     *
     * @param prefix Configuration file prefix.
     */
    public TopologyInit(String prefix) {
        this.prefix = prefix;
        this.linkPid = Configuration.getPid(prefix + "." + PAR_LINK);
    }

    protected Graph getGraph(){
        return new OverlayGraph(this.linkPid, false);
    }
}
