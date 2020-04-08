package peersim.dht.topology.gml;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.nio.charset.Charset;

/**
 * A reader for the Graph Modelling Language (GML).
 *
 * GML definition taken from
 * (http://www.fim.uni-passau.de/fileadmin/files/lehrstuhl/brandenburg/projekte/gml/gml-documentation.tar.gz)
 *
 * It's not clear that all node have to have id's or that they have to be integers - we assume that this is the case. We
 * also assume that only one graph can be defined in a file.
 *
 * @author Stuart Hendren (http://stuarthendren.net)
 * @author Stephen Mallette
 */
public class GMLReader {

    /**
     * Load the GML file into the Graph.
     *
     * @param filename         GML file
     * @throws IOException thrown if the data is not valid
     */
    public static void inputGraph(final String filename, final int pId, final int topologyId) throws IOException {
        FileInputStream fis = new FileInputStream(filename);
        GMLReader.inputGraph(fis, pId, topologyId);
        fis.close();
    }

    /**
     * Load the GML file into the Graph.
     *
     * @param inputStream      GML file
     * @throws IOException thrown if the data is not valid
     */
    public static void inputGraph(final InputStream inputStream, final int pId, final int topologyId) throws IOException {

        final Reader r = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("ISO-8859-1")));
        final StreamTokenizer st = new StreamTokenizer(r);

        try {
            st.commentChar(GMLTokens.COMMENT_CHAR);
            st.ordinaryChar('[');
            st.ordinaryChar(']');

            final String stringCharacters = "/\\(){}<>!£$%^&*-+=,.?:;@_`|~";
            for (int i = 0; i < stringCharacters.length(); i++) {
                st.wordChars(stringCharacters.charAt(i), stringCharacters.charAt(i));
            }

            new GMLParser(pId, topologyId).parse(st);

        } catch (IOException e) {
            throw new IOException("GML malformed line number " + st.lineno() + ": ", e);
        } finally {
            r.close();
        }
    }
}
