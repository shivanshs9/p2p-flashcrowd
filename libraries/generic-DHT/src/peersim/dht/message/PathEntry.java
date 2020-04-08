package peersim.dht.message;

import org.json.simple.JSONObject;
import peersim.core.Node;
import peersim.dht.DHTProtocol;

/**
 * Created by baumeist on 5/21/17.
 */
public class PathEntry extends Entry{
    public final DHTMessage.MessageStatus status;

    public PathEntry(Node n, DHTProtocol dhtNode, int hop, DHTMessage.MessageStatus s){
        super(n, dhtNode, hop);
        this.status = s;
    }

    public String toString(){
        StringBuilder b = new StringBuilder();
        b.append(super.toString());
        b.append('(').append(lookupStatus(this.status)).append('-').append(this.hop).append(')');
        return b.toString();
    }

    public JSONObject toJSON(){
        JSONObject entry = super.toJSON();
        entry.put("status", this.status.toString());
        return entry;
    }

    private String lookupStatus(DHTMessage.MessageStatus status){
        return status.toString().substring(0,2);
    }
}
