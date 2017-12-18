package report;

import core.DTNHost;
import core.Message;
import core.Settings;
import core.UpdateListener;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.reliability.EpidemicActiveRouter;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Michael Donny Kusuma
 */
public class NodeRecoverReport extends Report implements UpdateListener{
    public static final String REPORT_INTERVAL = "Interval";
    public static final int DEFAULT_REPORT_INTERVAL = 86400;
    public static final int DEFAULT_NO_MESSAGE_REPORTED = 500;
    public static final int DEFAULT_MAX_COUNTER = 1000;
    private double lastRecord = Double.MIN_VALUE;
    private int interval;
    private Map<String, Map<Integer, Integer>> recover = new HashMap<String, Map<Integer, Integer>>();
    private int updateCounter = 0;

    public NodeRecoverReport() {
        super();
        Settings settings = getSettings();
        if (settings.contains(REPORT_INTERVAL)) {
            interval = settings.getInt(REPORT_INTERVAL);
        } else {
            interval = -1;
        }
        if (interval < 0) {
            interval = DEFAULT_REPORT_INTERVAL;
        }

        initRecover();
    }
    @Override
    public void updated(List<DTNHost> hosts) {
        double simTime = getSimTime();
        if (isWarmup()) {
            return;
        }
        if ((simTime - lastRecord >= interval)&&(updateCounter <= DEFAULT_MAX_COUNTER)) {
            printLine(hosts);
            updateCounter++;
            this.lastRecord = simTime - simTime % interval;
        }
    }
    private void printLine(List<DTNHost> hosts) {
        for (DTNHost host : hosts) {

            EpidemicActiveRouter hostRouter = this.getRouter(host);
            Map<String, DTNHost> receiptBuffer = hostRouter.getReceiptBuffer();

            for (Map.Entry<String, Map<Integer, Integer>> sMessage : recover.entrySet()){

                if (receiptBuffer.containsKey(sMessage.getKey())){
                    Map<Integer, Integer> temp = recover.get(sMessage.getKey());
                    if (temp.containsKey(updateCounter)) {
                        temp.replace(updateCounter, (temp.get(updateCounter)+1));
                    } else {
                        temp.put(updateCounter, 1);
                    }
                    recover.replace(sMessage.getKey(), temp);
                }
            }
        }
    }
    @Override
    public void done() {
        for (Map.Entry<String, Map<Integer, Integer>> entry : recover.entrySet()) {
            Map<Integer, Integer> value = entry.getValue();
            for (int i = 0; i < updateCounter; i++) {
                if (value.containsKey(i)) {
                    continue;
                }
                value.put(i, 0);
            }
            recover.replace(entry.getKey(), value);
        }
        for (Map.Entry<String, Map<Integer, Integer>> entry : recover.entrySet()) {
            String printHost = entry.getKey()+"";
            Map<Integer, Integer> value = entry.getValue();
            for (Map.Entry<Integer, Integer> values : value.entrySet()) {
                printHost = printHost +"\t"+values.getValue();
            }
            write(printHost);
        }
	super.done();
    }

    private void initRecover(){
        for (int i=1; i <= DEFAULT_NO_MESSAGE_REPORTED; i++){
            Map<Integer, Integer> temp;
            temp = new HashMap<>();
            temp.put(updateCounter, 0);
            recover.put("M"+i, temp);
        }
    }

    private EpidemicActiveRouter getRouter(DTNHost peer) {
        MessageRouter otherRouter = peer.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works with other routers of same type";
        return (EpidemicActiveRouter) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

}