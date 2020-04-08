package peersim.dht.observer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import peersim.core.CommonState;
import peersim.util.IncrementalFreq;

import java.io.PrintStream;

/**
 * Created by baumeist on 8/27/17.
 */
public class GlobalStatsObserver extends DHTPrinter {

    private static IncrementalFreq routingChoiceFreq = new IncrementalFreq();
    private static int networkChurnCount = 0;

    public GlobalStatsObserver(String prefix){
        super(prefix, ".stats");
    }

    public static void addRoutingChoice(int choiceNumber){
        GlobalStatsObserver.routingChoiceFreq.add(choiceNumber);
    }

    public static void networkConnectionChanged(){
        GlobalStatsObserver.networkChurnCount++;
    }

    public static int getNetworkChurnCount(){ return GlobalStatsObserver.networkChurnCount; }


    @Override
    protected void writeData(PrintStream out) {
        JSONObject stats = new JSONObject();

        // Add routing choice frequency
        JSONArray routingFreq = new JSONArray();
        int lastBucket = this.getLastFrequencyWithData(GlobalStatsObserver.routingChoiceFreq);
        int alreadyCounted = 0;
        for(int i = lastBucket; i > 0; i--){
            // hack to correct double counted routing choices
            int count = GlobalStatsObserver.routingChoiceFreq.getFreq(i) - alreadyCounted;
            alreadyCounted += count;

            JSONObject routingChoiceFreq = new JSONObject();
            routingChoiceFreq.put("choice", i);
            routingChoiceFreq.put("frequency", count);
            routingFreq.add(routingChoiceFreq);
        }

        stats.put("cycle", CommonState.getTime());
        stats.put("routing_choice_frequency", routingFreq);
        stats.put("churn_count", GlobalStatsObserver.networkChurnCount);
        out.print(stats.toJSONString());
    }

    private int getLastFrequencyWithData(IncrementalFreq freq){
        int i = 0;
        while(true){
            if(freq.getFreq(i+1) == 0)
                break;
            i++;
        }
        return i;
    }
}
