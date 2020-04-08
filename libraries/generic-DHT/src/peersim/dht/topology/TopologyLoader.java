package peersim.dht.topology;

import peersim.config.Configuration;
import peersim.dht.topology.gml.GMLReader;
import peersim.dht.utils.DHTControlWithProtocol;

import java.io.File;

/**
 * Load a topology from a .GML file into a {@link peersim.core.Linkable} object. The links in the GML file will
 * be added to any links already present in the given topology.
 */
public class TopologyLoader extends DHTControlWithProtocol {

    /**
     * The {@value #PAR_TOPOLOGY} configuration parameter which references the
     * {@link peersim.core.Linkable} topology object.
     *
     * @config
     */
    protected static final String PAR_TOPOLOGY = "topology";

    /**
     * Stores the topology protocol identifier.
     */
    protected final int topologyPid;

    /**
     * The {@value #PAR_GML_FILE} configuration parameter which references the
     * file path the GML file to load the topology from.
     *
     * @config
     */
    protected static final String PAR_GML_FILE = "gml_file";

    /**
     * Stores the topology protocol identifier.
     */
    protected final File gmlFile;

    /**
     * @param prefix Configuration prefix
     */
    public TopologyLoader(String prefix){
        super(prefix);
        this.topologyPid = Configuration.getPid(this.prefix + "." + PAR_TOPOLOGY);
        this.gmlFile = new File(Configuration.getString(this.prefix + "." + PAR_GML_FILE));
        if(!this.gmlFile.exists()){
            System.err.println(String.format("Unable to find the GML file: %s", this.gmlFile.getAbsolutePath()));
            System.exit(5); // abort
        }
    }

    @Override
    public boolean execute() {
        try {
            GMLReader.inputGraph(this.gmlFile.getAbsolutePath(), this.pid, this.topologyPid);
        }catch (Exception ex){
            System.err.println(ex.getMessage());
            System.exit(5); // abort
        }
        return true;
    }
}
