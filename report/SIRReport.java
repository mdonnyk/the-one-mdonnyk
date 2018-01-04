package report;

import core.DTNHost;
import core.Message;
import core.Settings;
import core.UpdateListener;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.reliability.EpidemicActiveRouter;
import routing.reliability.EpidemicPassiveRouter;

import java.util.*;

/**
 *
 * @author Michael Donny Kusuma
 */
public class SIRReport extends Report implements UpdateListener{
    public static final String REPORT_INTERVAL = "Interval";
    public static final int DEFAULT_REPORT_INTERVAL = 3600 ;


    private double lastRecord;
    private String messageReported = "M209";

    private Map<DTNHost, Boolean> nodeState = new HashMap<>();

    private List<Integer> nodeInfective = new ArrayList<>();
    private List<Integer> nodeSuspected = new ArrayList<>();
    private List<Integer> nodeRemoved = new ArrayList<>();

    private int interval;
    private int updateCounter = 0;

    public SIRReport() {
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
            if (lastRecord>0){

                if ((simTime - lastRecord >= interval)) {
                    //compute host carry message

                    computeNumberofHosts(hosts , messageReported);

                    lastRecord = simTime - simTime % interval;
                }
            }
            else {
                findInitTime(hosts, messageReported);
            }

    }

    private void findInitTime(List<DTNHost> hosts, String idMessage){

        for (DTNHost host : hosts){
            for (Message m : host.getMessageCollection()){
                if (idMessage.equals(m.getId())){
                    lastRecord = m.getCreationTime();
                }
            }
        }
    }

    private void computeNumberofHosts(List<DTNHost> hosts, String idMessage){
        int s = 0;
        int i = 0;
        int r = 0;

        for (DTNHost host : hosts){

           /* // Initialization of state
            if (!nodeState.containsKey(host)){

                // set to "susceptible"
                nodeState.put(host, -1);
            }

            // Checking state
            boolean currentState = false;
            for (Message m : host.getMessageCollection()){
                if (idMessage.equals(m.getId())){
                    currentState = true;
                }
            }

            // Deciding current state
            if (currentState){
                // set to "infected"
                nodeState.put(host, 1);
            }

            else {
                if (nodeState.get(host)<0){
                    // set to removed
                    nodeState.put(host,0);
                }
            }


            // count state
            if (nodeState.get(host)<0){
                //s++;
            }
            else if (nodeState.get(host)>0){
                i++;
            }
            else if (nodeState.get(host)==0){
                //r++;
            }
*/
           //init
           if (!nodeState.containsKey(host)){


                nodeState.put(host, false);
            }

           boolean infected = false;
           for (Message m : host.getMessageCollection()){
                if (idMessage.equals(m.getId())){
                    infected = true;
                }
            }

            if (infected){
               i++;

               if (!nodeState.get(host)){
                   nodeState.put(host, true);
               }

            }
            else {
                if (nodeState.get(host)){
                    r++;
                }
                else {
                    s++;
                }
            }
        }
        nodeInfective.add(i);
        nodeSuspected.add(s);
        nodeRemoved.add(r);

    }


    @Override
    public void done() {
        String print = "SIR Report";
        print = print +"\nS";

        print = print +"\n"+messageReported;

        for (int val : nodeSuspected){
            print = print +"\t"+ val;
        }

        print = print +"\nI";
        print = print +"\n"+messageReported;

        for (int val : nodeInfective){
            print = print +"\t"+ val;
        }

        print = print +"\nR";
        print = print +"\n"+messageReported;

        for (int val : nodeRemoved){
            print = print +"\t"+ val;
        }
        write(print);


	super.done();
    }

    // Create init object for message that reported
    private void initInfective(){
        lastRecord = -1;
    }

    private EpidemicPassiveRouter getRouter(DTNHost peer) {
        MessageRouter otherRouter = peer.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works with other routers of same type";
        return (EpidemicPassiveRouter) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

}