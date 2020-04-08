package peersim.dht.topology;

import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.DynamicNetwork;
import peersim.dynamics.NodeInitializer;

/**
 * Created by baumeist on 5/21/17.
 */
public class MoreDynamicNetwork extends DynamicNetwork {

    /**
     * Config parameter which gives the prefix of node initializers. Run after node is added to the network.
     * An arbitrary
     * number of node initializers can be specified (Along with their parameters).
     * These will be applied
     * on the newly created nodes. The initializers are ordered according to
     * alphabetical order if their ID.
     * Example:
     * <pre>
     control.0 DynamicNetwork
     control.0.post.0 RandNI
     control.0.post.0.k 5
     control.0.post.0.protocol somelinkable
     ...
     * </pre>
     * @config
     */
    private static final String PAR_POST = "post";

    /** node initializers to apply on the newly added nodes (after they have been added to the network */
    protected final NodeInitializer[] posts;

    public MoreDynamicNetwork(String prefix){
        super(prefix);

        // Load the post setup initializers
        Object[] tmp = Configuration.getInstanceArray(prefix + "." + PAR_POST);
        this.posts = new NodeInitializer[tmp.length];
        for (int i = 0; i < tmp.length; ++i) {
            this.posts[i] = (NodeInitializer) tmp[i];
        }
    }

    @Override
    protected void add(int n)
    {
        for (int i = 0; i < n; ++i) {
            Node newNode = (Node) Network.prototype.clone();
            for (int j = 0; j < this.inits.length; ++j) {
                this.inits[j].initialize(newNode);
            }
            Network.add(newNode);

            for (int j = 0; j < this.posts.length; ++j) {
                this.posts[j].initialize(newNode);
            }
        }
    }
}
