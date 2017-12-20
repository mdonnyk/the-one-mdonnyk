package report;

import core.DTNHost;
import core.Message;
import core.Settings;
import core.UpdateListener;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.reliability.EpidemicActiveRouter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Michael Donny Kusuma
 */
public class NodeSusceptibleReport2 extends Report implements UpdateListener{
    public static final String REPORT_INTERVAL = "Interval";
    public static final int DEFAULT_REPORT_INTERVAL = 3600;
    public static final int DEFAULT_NO_MESSAGE_REPORTED = 50;
    public static final int DEFAULT_MAX_COUNTER = 1000;
    private Map<String, Double> lastRecord = new HashMap<>();
    private Map<String, List<Integer>> nodeSusceptible = new HashMap<String, List<Integer>>();
    private int interval;
    //private int updateCounter = 0;

    public NodeSusceptibleReport2() {
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
        for (Map.Entry<String, List<Integer>> entry : nodeSusceptible.entrySet()){

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

            EpidemicActiveRouter hostRouter = this.getRouter(host);
            Map<String, DTNHost> receiptBuffer = hostRouter.getReceiptBuffer();

            for (Message m : host.getMessageCollection()){
                if (idMessage.equals(m.getId()) && !receiptBuffer.containsKey(m.getId()) ){
                    temp++;
                }
            }
        }

        List<Integer> tempList = nodeSusceptible.get(idMessage);
        tempList.add(temp);

    }


    @Override
    public void done() {


        for (Map.Entry<String, List<Integer>> entry : nodeSusceptible.entrySet()){
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
            nodeSusceptible.put("M"+i, temp);
            lastRecord.put("M"+i, -1.0);
        }
    }

    private EpidemicActiveRouter getRouter(DTNHost peer) {
        MessageRouter otherRouter = peer.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works with other routers of same type";
        return (EpidemicActiveRouter) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

}