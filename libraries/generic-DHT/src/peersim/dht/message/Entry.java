package peersim.dht.message;

import org.json.simple.JSONObject;
import peersim.core.Node;
import peersim.dht.DHTProtocol;

/**
 * Created by baumeist on 5/21/17.
 */
public class Entry {
    public Node node;
    public DHTProtocol dht;
    public int hop = -1;

    public Entry(Node n, DHTProtocol dhtNode, int hop){
        this.node = n;
        this.dht = dhtNode;
        this.hop = hop;
    }

    public String toString(){
        StringBuilder b = new StringBuilder();
        b.append(this.node.getID());
        return b.toString();
    }

    public JSONObject toJSON(){
        JSONObject entry = new JSONObject();
        entry.put("id", this.node.getID());
        entry.put("address", this.dht.getAddress());
        entry.put("is_adversary", this.dht.isAdversary());
        entry.put("hop", this.hop);
        return entry;
    }
}
