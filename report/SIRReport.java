package report;

import core.DTNHost;
import core.Message;
import core.Settings;
import core.UpdateListener;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.reliability.EpidemicActiveRouter;
import routing.reliability.EpidemicPassiveRouter;
import routing.reliability.EpidemicPassiveWTombstoneRouter;

import java.util.*;

/**
 *
 * @author Michael Donny Kusuma
 * Sanata Dharma University
 */
public class SIRReport extends Report implements UpdateListener{
    public static final String REPORT_INTERVAL = "Interval";
    //1 jam
    public static int DEFAULT_REPORT_INTERVAL = 3600 ;
    private double lastRecord;

    // Message need to reported
    private String messageReported = "M1";

    // Status node to the message, true if node is infected by the message
    private Map<DTNHost, Boolean> nodeState = new HashMap<>();



    private List<Integer> nodeInfective = new ArrayList<>();
    private List<Integer> nodeSuspected = new ArrayList<>();
    private List<Integer> nodeRemoved = new ArrayList<>();

    private int ROUTING_ENGINE = -1;

    private int interval;

    public SIRReport() {
        super();
        Settings routeSettings = new Settings("DecisionEngineRouter");
        Settings SIRSettings = new Settings("SIR");

        Settings settings = getSettings();
        if (settings.contains(REPORT_INTERVAL)) {
            interval = settings.getInt(REPORT_INTERVAL);
        } else {
            interval = -1;
        }
        if (interval < 0) {
            interval = DEFAULT_REPORT_INTERVAL;
        }

        String routingEngine = routeSettings.getSetting("decisionEngine");

        if (routingEngine.equals("reliability.EpidemicActiveRouter")){
            ROUTING_ENGINE = 1;
        }
        else if (routingEngine.equals("reliability.EpidemicPassiveRouter")){
            ROUTING_ENGINE = 2;
        }
        else if (routingEngine.equals("reliability.EpidemicPassiveWTombstoneRouter")){
            ROUTING_ENGINE = 3;
        }

        messageReported = "M" + SIRSettings.getSetting("numberOfMessage");

        DEFAULT_REPORT_INTERVAL = Integer.parseInt(SIRSettings.getSetting("interval"));

        initInfective();
    }
    @Override
    public void updated(List<DTNHost> hosts) {
        double simTime = getSimTime();
            if (lastRecord>0){

                if ((simTime - lastRecord >= interval)) {
                    //compute host carry message

                    if (ROUTING_ENGINE==1){
                        activeDecider(hosts , messageReported);
                    }
                    else if (ROUTING_ENGINE==2){
                        passiveDecider(hosts , messageReported);
                    }
                    else if (ROUTING_ENGINE==3){
                        passiveWTombstoneDecider(hosts , messageReported);
                    }

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

    public void activeDecider(List<DTNHost> hosts, String idMessage){
        int s = 0;
        int i = 0;
        int r = 0;

        for (DTNHost host : hosts){

            EpidemicActiveRouter thisRouter = this.getActiveRouter(host);
            Map<String, DTNHost> receiptBuffer = thisRouter.getReceiptBuffer();

            boolean infected = false;
            for (Message m : host.getMessageCollection()){
                if (idMessage.equals(m.getId())){
                    infected = true;
                }
            }

            if (infected){
                i++;
            }
            else {
                if (receiptBuffer.containsKey(idMessage)){
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

    public void passiveDecider(List<DTNHost> hosts, String idMessage){
        int s = 0;
        int i = 0;
        int r = 0;

        for (DTNHost host : hosts){

            EpidemicPassiveRouter thisRouter = this.getPassiveRouter(host);
            Set<String> receiptBuffer = thisRouter.getReceiptBuffer();


            boolean infected = false;
            for (Message m : host.getMessageCollection()){
                if (idMessage.equals(m.getId())){
                    infected = true;
                }
            }

            if (infected){
                i++;
            }
            else if (!infected && receiptBuffer.contains(idMessage)){
                r++;
            }
            else {
                s++;
            }

        }

        nodeInfective.add(i);
        nodeSuspected.add(s);
        nodeRemoved.add(r);

    }

    public void passiveWTombstoneDecider(List<DTNHost> hosts, String idMessage){
        int s = 0;
        int i = 0;
        int r = 0;

        for (DTNHost host : hosts){

            EpidemicPassiveWTombstoneRouter thisRouter = this.getPassiveWTombstoneRouter(host);
            Set<String> receiptBuffer = thisRouter.getReceiptBuffer();
            Set<String> tombstone = thisRouter.getTombstone();

            boolean infected = false;
            for (Message m : host.getMessageCollection()){
                if (idMessage.equals(m.getId())){
                    infected = true;
                }
            }

            if (infected){
                i++;
            }

            else {
                if (receiptBuffer.contains(idMessage) || tombstone.contains(idMessage)){
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

    private EpidemicActiveRouter getActiveRouter (DTNHost host) {
        MessageRouter otherRouter = host.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works with other routers of same type";
        return (EpidemicActiveRouter) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    private EpidemicPassiveRouter getPassiveRouter (DTNHost host) {
        MessageRouter otherRouter = host.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works with other routers of same type";
        return (EpidemicPassiveRouter) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    private EpidemicPassiveWTombstoneRouter getPassiveWTombstoneRouter (DTNHost host) {
        MessageRouter otherRouter = host.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works with other routers of same type";
        return (EpidemicPassiveWTombstoneRouter) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

}