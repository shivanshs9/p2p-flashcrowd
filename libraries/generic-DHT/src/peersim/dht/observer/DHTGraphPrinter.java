package peersim.dht.observer;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.dht.DHTProtocol;
import peersim.graph.Graph;
import peersim.reports.GraphObserver;
import peersim.util.FileNameGenerator;

import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Prints a DHT Protocol graph in GML format.
 * 
 * @author todd
 * 
 */
public class DHTGraphPrinter extends GraphObserver {

	/**
	 * This is the prefix of the filename where the graph is saved. The
	 * extension is ".gml" and after the prefix the basename contains a numeric
	 * index that is incremented at each saving point. If not given, the graph
	 * is dumped on the standard output.
	 * 
	 * @config
	 */
	private static final String PAR_BASENAME = "outf";

	/**
	 * This is the reference to the DHT Protocol.
	 * {@link peersim.dht.DHTProtocol}
	 * 
	 * @config
	 */
	private static final String PAR_DHT = "dht";

	private final String baseName;

	private final FileNameGenerator fng;

	private final int did;

	/**
	 * Constructor that reads the configuration parameters.
	 * 
	 * @param prefix
	 *            Configuration prefix
	 */
	public DHTGraphPrinter(String prefix) {
		super(prefix);
		this.did = Configuration.getPid(prefix + "." + PAR_DHT);
		this.baseName = Configuration.getString(prefix + "." + PAR_BASENAME,
				null);
		if (this.baseName != null)
			this.fng = new FileNameGenerator(baseName, ".gml");
		else
			this.fng = null;
	}

	/**
	 * Called by the Simulation scheduler.
	 */
	public boolean execute() {
		try {
			updateGraph();

			System.out.print(name + ": ");

			// initialize output streams
			FileOutputStream fos = null;
			PrintStream pstr = System.out;
			if (baseName != null) {
				String fname = fng.nextCounterName();
				File file = new File(fname);
				file.getParentFile().mkdirs();
				fos = new FileOutputStream(file);
				System.out.println("writing to file " + fname);
				pstr = new PrintStream(fos);
			} else
				System.out.println();

			writeGML(g, pstr);

			if (fos != null)
				fos.close();

			return false;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Saves the given graph to the given stream in GML format.
	 */
	private void writeGML(Graph g, PrintStream out) {

		out.println("graph [");
		out.println("\tdirected " + (g.directed() ? "1" : "0"));
		out.println("\tcycle " + CommonState.getTime());
		out.println("\tbeginningChurnCount " + GlobalStatsObserver.getNetworkChurnCount());


		for (int i = 0; i < g.size(); ++i) {
			Node n = (Node) g.getNode(i);
			DHTProtocol dhtNode = (DHTProtocol) n.getProtocol(this.did);
			out.println("\tnode [");
			out.println("\t\tid " + n.getID());
			out.println("\t\tlabel \"" + dhtNode.getAddress() + "\"");
			out.println("\t\tlocation " + dhtNode.getAddress());
			out.println("\t\tadversary " + (dhtNode.isAdversary()?1:0));
			out.println("\t]");
		}

		for (int i = 0; i < g.size(); ++i) {
			Node n = (Node) g.getNode(i);
			Iterator<Integer> it = g.getNeighbours(i).iterator();
			while (it.hasNext()) {
				Node n2 = (Node) g.getNode(it.next());
				// only write edges once ... so
				if(n.getID() > n2.getID())
					continue;
				out.println("edge [ source " + n.getID() + " target "
						+ n2.getID() + " ]");
			}
		}

		out.println("]");
	}

}
