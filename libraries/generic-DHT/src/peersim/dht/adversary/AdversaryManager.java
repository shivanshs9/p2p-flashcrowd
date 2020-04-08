package peersim.dht.adversary;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dht.DHTProtocol;
import peersim.dht.utils.DHTControlWithProtocol;

/**
 * Randomly assigns nodes as adversaries in the given topology.
 */
public class AdversaryManager extends DHTControlWithProtocol {

    /**
     * The absolute or percent value of malicious node to introduce into the topology
     */
    private final String count;

    /**
     * The {@value #PAR_COUNT} configuration parameter which defines how many malicious node to introduce into the
     * topology. For a percent value, append % after the number. For an absolute value, just enter integer.
     *
     * @config
     */
    protected static final String PAR_COUNT = "count";


    public AdversaryManager(String prefix) throws Exception{
        super(prefix);
        this.count = Configuration.getString(this.prefix + "." + PAR_COUNT).trim();

        // check that the count is valid
        if(0 > this.getCount())
            throw new Exception("Invalid number of adversaries to add into the topology");
    }

    private boolean isPercent(){
        return this.count.endsWith("%");
    }

    private int getCount(){
        return Integer.parseInt( this.count.replace("%", ""));
    }

    @Override
    public boolean execute() {
        int size = Network.size();
        int adversaryCount = 0;
        if(this.isPercent())
            adversaryCount = (int)Math.ceil((this.getCount() / 100.0) * size);
        else
            adversaryCount = this.getCount();

        int addedCount = 0;
        while(addedCount < adversaryCount && addedCount < size){
            // pick a random node an set it as an adversary
            Node adversary = Network.get(CommonState.r.nextInt(size));
            DHTProtocol dht = (DHTProtocol)adversary.getProtocol(this.pid);
            if(dht.isAdversary())
                continue;  // already an adversary continue

            dht.setAdversary(true);
            addedCount++;
        }

        return true;
    }
}
