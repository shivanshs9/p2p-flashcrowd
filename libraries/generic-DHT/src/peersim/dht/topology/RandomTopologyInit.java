package peersim.dht.topology;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.graph.Graph;

/**
 * Created by baumeist on 5/21/17.
 */
public class RandomTopologyInit extends TopologyInit {

    /**
     * The {@value #PAR_DEGREE} configuration parameter defines how many other nodes
     * to connect this node to.
     *
     * @config
     */
    private static final String PAR_DEGREE = "k";

    private final int k;

    public RandomTopologyInit(String prefix){
        super(prefix);
        this.k = Configuration.getInt(prefix + "." + "k");
    }

    @Override
    public void initialize(Node node) {
        // wire this node to k other random nodes
        // copy of the Graph Factory WireKOut methods (mostly)
        Graph g = this.getGraph();
        final int n = g.size();
        int myK = this.k;
        if( n < 2 ) return;
        if( n <= myK ) myK=n-1;

        int j=0;
        while(j<myK)
        {
            int newEdge = CommonState.r.nextInt(n);
            if( node.getIndex() != newEdge )
            {
                g.setEdge(node.getIndex(),newEdge);
                j++;
            }
        }
    }
}
