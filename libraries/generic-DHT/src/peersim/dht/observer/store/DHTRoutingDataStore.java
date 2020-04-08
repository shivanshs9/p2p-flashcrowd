package peersim.dht.observer.store;

import peersim.core.CommonState;
import peersim.dht.message.DHTMessage;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.*;
import org.json.simple.*;
import peersim.dht.observer.GlobalStatsObserver;

/**
 * Created by baumeist on 12/18/16.
 */
public class DHTRoutingDataStore {

    private final Logger logger;
    private final String outFormat = "%d,%d,%d,%d,%s,%s,%b,%d,%d,%s,%s\n";

    private static Hashtable<String, DHTRoutingDataStore> instances = new Hashtable<>();

    public static DHTRoutingDataStore getInstance(String filePath) throws SecurityException, IOException{
        // special string for stdout
        if (filePath.equals("-"))
            return addGetInstance(filePath);

        // normal file path
        File f = new File(filePath);
        return addGetInstance(f.getCanonicalPath());
    }

    private static DHTRoutingDataStore addGetInstance(String filePath) throws SecurityException, IOException{
        if(!DHTRoutingDataStore.instances.containsKey(filePath))
            DHTRoutingDataStore.instances.put(filePath, new DHTRoutingDataStore(filePath));
        return DHTRoutingDataStore.instances.get(filePath);
    }

    private DHTRoutingDataStore(String filePath) throws SecurityException, IOException {
        // Use the built in Java file logging class to write to file
        this.logger = Logger.getAnonymousLogger();
        this.logger.setUseParentHandlers(false);

        if (filePath.equals("-")){
            this.logger.setUseParentHandlers(true);
        } else {
            File f = new File(filePath);
            if(f.exists())
                f.delete();
            f.getParentFile().mkdirs();
            f.createNewFile();

            FileHandler fh = new FileHandler(filePath, true);
            this.logger.addHandler(fh);
            fh.setFormatter(new CustomRecordFormatter());
        }

    }

    public void storeData(DHTMessage m){
        // Dump the message
        JSONObject message = new JSONObject();
        message.put("id", m.getMessageID());
        message.put("ref_id", m.getRefMessageID());
        message.put("source_node", m.getSourceNode().getID());
        message.put("destination_node", m.getDestinationNode().getID());
        message.put("target", m.getTarget());
        message.put("message_type", m.getClass().toString());
        message.put("delivered", m.wasDelivered());
        message.put("routing_path", m.getRoutingPath().toJSON());
        message.put("connection_path", m.getConnectionPath().toJSON());
        message.put("churn_count", GlobalStatsObserver.getNetworkChurnCount());
        message.put("cycle", CommonState.getTime());

        this.logger.info(message.toJSONString() + "\n");
    }

    private class CustomRecordFormatter extends Formatter {

        @Override
        public String format(final LogRecord r) {
            return r.getMessage();
        }
    }
}
