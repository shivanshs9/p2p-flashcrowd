package peersim.dht.topology.gml;

import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dht.DHTProtocol;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.Map;

public class GMLParser {

    private final Map<Object, Object> mapGmlIdsToNetworkIndex = new HashMap<>();

    private final int pId;

    private final int topologyId;

    private boolean directed = false;

    public GMLParser(final int pId, final int topologyId) {
        this.pId = pId;
        this.topologyId = topologyId;
    }

    public void parse(final StreamTokenizer st) throws IOException {
        // clear out the current graph
        final int size = Network.size();
        for(int i =0; i < size;i++){
            Network.remove();
        }

        while (hasNext(st)) {
            int type = st.ttype;
            if (notLineBreak(type)) {
                final String value = st.sval;
                if (GMLTokens.GRAPH.equals(value)) {
                    parseGraph(st);
                    if (!hasNext(st)) {
                        return;
                    }
                }
            }
        }
        throw new IOException("Graph not complete");
    }

    private void parseGraph(final StreamTokenizer st) throws IOException {
        checkValid(st, GMLTokens.GRAPH);
        while (hasNext(st)) {
            // st.nextToken();
            final int type = st.ttype;
            if (notLineBreak(type)) {
                if (type == ']') {
                    return;
                } else {
                    final String key = st.sval;
                    if (GMLTokens.NODE.equals(key)) {
                        addNode(parseNode(st));
                    } else if (GMLTokens.EDGE.equals(key)) {
                        addEdge(parseEdge(st));
                    } else if (GMLTokens.DIRECTED.equals(key)) {
                        directed = parseBoolean(st);
                    } else {
                        // IGNORE
                        parseValue("ignore", st);
                    }
                }
            }
        }
        throw new IOException("Graph not complete");
    }

    private void addNode(final Map<String, Object> map) throws IOException {
        final Object id = map.remove(GMLTokens.ID);
        if (id != null) {
            Node n = (Node)Network.prototype.clone();
            Network.add(n); // sets index
            this.mapGmlIdsToNetworkIndex.put(id, n.getIndex());

            // set the location
            DHTProtocol dht = (DHTProtocol)n.getProtocol(this.pId);
            Object address = map.remove(GMLTokens.LOCATION);
            if (address == null)
                throw new IOException(String.format("%s of the node not found", GMLTokens.LOCATION));
            if (Integer.class.isInstance(address)){
                // convert to Double
                address = Double.valueOf((double)((Integer)address).intValue());
            }
            dht.setAddress((double)address);
        } else {
            throw new IOException("No id found for node");
        }
    }

    private void addEdge(final Map<String, Object> map) throws IOException {
        Object source = map.remove(GMLTokens.SOURCE);
        Object target = map.remove(GMLTokens.TARGET);

        if (source == null) {
            throw new IOException("Edge has no source");
        }

        if (target == null) {
            throw new IOException("Edge has no target");
        }

        // translate GML id to Network index
        source = this.mapGmlIdsToNetworkIndex.get(source);
        target = this.mapGmlIdsToNetworkIndex.get(target);

        Node outVertex = Network.get((int)source);
        Node inVertex = Network.get((int)target);

        if (outVertex == null) {
            throw new IOException("Edge source " + source + " not found");
        }
        if (inVertex == null) {
            throw new IOException("Edge target " + target + " not found");

        }

        // add link
        Linkable outVertexLink = (Linkable)outVertex.getProtocol(this.topologyId);
        Linkable inVertexLink = (Linkable)inVertex.getProtocol(this.topologyId);
        outVertexLink.addNeighbor(inVertex);
        if(!this.directed)
            inVertexLink.addNeighbor(outVertex);
    }

    private Object parseValue(final String key, final StreamTokenizer st) throws IOException {
        while (hasNext(st)) {
            final int type = st.ttype;
            if (notLineBreak(type)) {
                if (type == StreamTokenizer.TT_NUMBER) {
                    final Double doubleValue = Double.valueOf(st.nval);
                    if (doubleValue.equals(Double.valueOf(doubleValue.intValue()))) {
                        return doubleValue.intValue();
                    } else {
                        return doubleValue.doubleValue();
                    }
                } else {
                    if (type == '[') {
                        return parseMap(key, st);
                    } else if (type == '"') {
                        return st.sval;
                    }
                }
            }
        }
        throw new IOException("value not found");
    }

    private boolean parseBoolean(final StreamTokenizer st) throws IOException {
        while (hasNext(st)) {
            final int type = st.ttype;
            if (notLineBreak(type)) {
                if (type == StreamTokenizer.TT_NUMBER) {
                    return st.nval == 1.0;
                }
            }
        }
        throw new IOException("boolean not found");
    }

    private Map<String, Object> parseNode(final StreamTokenizer st) throws IOException {
        return parseElement(st, GMLTokens.NODE);
    }

    private Map<String, Object> parseEdge(final StreamTokenizer st) throws IOException {
        return parseElement(st, GMLTokens.EDGE);
    }

    private Map<String, Object> parseElement(final StreamTokenizer st, final String node) throws IOException {
        checkValid(st, node);
        return parseMap(node, st);
    }

    private Map<String, Object> parseMap(final String node, final StreamTokenizer st) throws IOException {
        final Map<String, Object> map = new HashMap<String, Object>();
        while (hasNext(st)) {
            final int type = st.ttype;
            if (notLineBreak(type)) {
                if (type == ']') {
                    return map;
                } else {
                    final String key = st.sval;
                    final Object value = parseValue(key, st);
                    map.put(key, value);
                }
            }
        }
        throw new IOException(node + " incomplete");
    }

    private void checkValid(final StreamTokenizer st, final String token) throws IOException {
        if (st.nextToken() != '[') {
            throw new IOException(token + " not followed by [");
        }
    }

    private boolean hasNext(final StreamTokenizer st) throws IOException {
        return st.nextToken() != StreamTokenizer.TT_EOF;
    }

    private boolean notLineBreak(final int type) {
        return type != StreamTokenizer.TT_EOL;
    }
}
