package report;

import core.DTNHost;
import core.Message;
import core.Settings;
import core.UpdateListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Michael Donny Kusuma
 */
public class NodeInfectedReport extends Report implements UpdateListener{
    public static final String REPORT_INTERVAL = "Interval";
    public static final int DEFAULT_REPORT_INTERVAL = 3600;
    public static final int DEFAULT_NO_MESSAGE_REPORTED = 500;
    public static final int DEFAULT_MAX_COUNTER = 1000;
    private double lastRecord = Double.MIN_VALUE;
    private int interval;
    private Map<String, Map<Integer, Integer>> infective = new HashMap<String, Map<Integer, Integer>>();
    private int updateCounter = 0;

    public NodeInfectedReport() {
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

        initInfective();
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

            for (Message m : host.getMessageCollection()) {
                Map<Integer, Integer> temp;
                if (infective.containsKey(m.getId())) {
                    temp = infective.get(m.getId());
                    if (temp.containsKey(updateCounter)) {
                        temp.replace(updateCounter, (temp.get(updateCounter)+1));
                    } else {
                        temp.put(updateCounter, 1);
                    }
                    infective.replace(m.getId(), temp);
                } /*else {
                    temp = new HashMap<>();
                    temp.put(updateCounter, 1);
                    infective.put(m.getId(), temp);
                }*/
            }
        }
    }
    @Override
    public void done() {
        for (Map.Entry<String, Map<Integer, Integer>> entry : infective.entrySet()) {
            Map<Integer, Integer> value = entry.getValue();
            for (int i = 0; i < updateCounter; i++) {
                if (value.containsKey(i)) {
                    continue;
                }
                value.put(i, 0);
            }
            infective.replace(entry.getKey(), value);
        }
        for (Map.Entry<String, Map<Integer, Integer>> entry : infective.entrySet()) {
            String printHost = entry.getKey()+"";
            Map<Integer, Integer> value = entry.getValue();
            for (Map.Entry<Integer, Integer> values : value.entrySet()) {
                printHost = printHost +"\t"+values.getValue();
            }
            write(printHost);
        }
	super.done();
    }

    private void initInfective(){
        for (int i=1; i <= DEFAULT_NO_MESSAGE_REPORTED; i++){
            Map<Integer, Integer> temp;
            temp = new HashMap<>();
            temp.put(updateCounter, 0);
            infective.put("M"+i, temp);
        }
    }

}