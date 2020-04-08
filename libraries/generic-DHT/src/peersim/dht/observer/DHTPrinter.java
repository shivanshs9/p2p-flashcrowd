package peersim.dht.observer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import peersim.config.Configuration;
import peersim.dht.utils.DHTControlWithProtocol;
import peersim.util.FileNameGenerator;

/**
 * Base class for controls that can write to a file.
 * @author todd
 *
 */
public abstract class DHTPrinter extends DHTControlWithProtocol{
	
	/**
	 * This is the prefix of the filename where the graph is saved. The
	 * extension is ".csv" and after the prefix the basename contains a numeric
	 * index that is incremented at each saving point. If not given, the graph
	 * is dumped on the standard output.
	 * 
	 * @config
	 */
	private static final String PAR_BASENAME = "outf";

	private final String baseName;
	private final FileNameGenerator fng;

	/**
	 * Default constructor
	 * @param prefix 
	 * 			configuration name.
	 * @param fileExt
	 * 			file extension to use.
	 */
	public DHTPrinter(String prefix, String fileExt) {
		super(prefix);
		this.baseName = Configuration.getString(prefix + "." + PAR_BASENAME,
				null);
		if (this.baseName != null)
			this.fng = new FileNameGenerator(baseName, fileExt);
		else
			this.fng = null;
	}
	
	@Override
	public boolean execute() {
		try {
			System.out.print(this.prefix + ": ");

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

			this.writeData(pstr);

			if (fos != null)
				fos.close();

			return false;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected abstract void writeData(PrintStream out);

}
