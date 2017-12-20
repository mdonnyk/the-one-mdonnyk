package report;

import core.DTNHost;
import core.Message;
import core.Settings;
import core.UpdateListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Michael Donny Kusuma
 */
public class NodeInfectedReport2 extends Report implements UpdateListener{
    public static final String REPORT_INTERVAL = "Interval";
    public static final int DEFAULT_REPORT_INTERVAL = 3600;
    public static final int DEFAULT_NO_MESSAGE_REPORTED = 50;
    public static final int DEFAULT_MAX_COUNTER = 1000;
    private Map<String, Double> lastRecord = new HashMap<>();
    private Map<String, List<Integer>> nodeInfective = new HashMap<String, List<Integer>>();
    private int interval;
    //private int updateCounter = 0;

    public NodeInfectedReport2() {
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


        // check for all observed message, if update is above interval
        for (Map.Entry<String, List<Integer>> entry : nodeInfective.entrySet()){

            if (lastRecord.get(entry.getKey())>0){

                if ((simTime - lastRecord.get(entry.getKey()) >= interval)) {
                    //compute host carry message

                    computeNumberofHosts(hosts , entry.getKey());
                    double lastRecordUpdated = simTime - simTime % interval;
                    lastRecord.put(entry.getKey(), lastRecordUpdated);
                }
            }
            else {
                findInitTime(hosts, entry.getKey());
            }


        }



    }

    private void findInitTime(List<DTNHost> hosts, String idMessage){

        for (DTNHost host : hosts){
            for (Message m : host.getMessageCollection()){
                if (idMessage.equals(m.getId())){
                    lastRecord.put(idMessage, m.getCreationTime());
                }
            }
        }
    }

    private void computeNumberofHosts(List<DTNHost> hosts, String idMessage){
        int temp = 0;

        for (DTNHost host : hosts){
            for (Message m : host.getMessageCollection()){
                if (idMessage.equals(m.getId())){
                    temp++;
                }
            }
        }

        List<Integer> tempList = nodeInfective.get(idMessage);
        tempList.add(temp);

    }


    @Override
    public void done() {


        for (Map.Entry<String, List<Integer>> entry : nodeInfective.entrySet()){
            String print = entry.getKey()+"\t";
            List<Integer> value = entry.getValue();
            for (int val : value){
                print = print +"\t"+ val;
            }
        write(print);
        }

	super.done();
    }

    private void initInfective(){
        for (int i=1; i <= DEFAULT_NO_MESSAGE_REPORTED; i++){
            List<Integer> temp;
            temp = new ArrayList<>();
            nodeInfective.put("M"+i, temp);
            lastRecord.put("M"+i, -1.0);
        }
    }

}