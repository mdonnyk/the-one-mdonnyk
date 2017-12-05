package report;

import core.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeliveryIntervalReport extends Report implements MessageListener, UpdateListener {

    /**
     * Record occupancy every nth second -setting id ({@value}).
     * Defines the interval how often (seconds) a new snapshot of buffer
     * occupancy is taken previous:5
     */
    public static final String BUFFER_REPORT_INTERVAL = "occupancyInterval";
    /** Default value for the snapshot interval */
    public static final int DEFAULT_BUFFER_REPORT_INTERVAL = 3600;
    private double lastRecord = Double.MIN_VALUE;
    private int interval;
    private int updateCounter = 0;
    private int temp = 0;
    private int temp2 = 0;
    private Map<Integer, Integer> nrofDelivered = new HashMap<Integer, Integer>();
    private Map<Integer, Integer> nrofCreated = new HashMap<Integer, Integer>();

    public DeliveryIntervalReport() {
        super();
        
        Settings settings = getSettings();
        if (settings.contains(BUFFER_REPORT_INTERVAL)) {
            interval = settings.getInt(BUFFER_REPORT_INTERVAL);
        } else {
            interval = -1; /* not found; use default */
        }
        
        if (interval < 0) { /* not found or invalid value -> use default */
            interval = DEFAULT_BUFFER_REPORT_INTERVAL;
        }
    }
    
    public void updated(List<DTNHost> hosts) {
        double simTime = getSimTime();
        if (isWarmup()) {
            return;
        }
        
        if (simTime - lastRecord >= interval) {

            temp=temp+nrofCreated.get(updateCounter);
            temp2=temp2+nrofDelivered.get(updateCounter);

            double deliveryProb = 0;
            if (this.nrofDelivered.get(updateCounter) > 0) {
                //System.out.println("Created : "+nrofCreated.get(updateCounter));
                //System.out.println("Delivered : "+nrofDelivered.get(updateCounter));
                deliveryProb = (1.0 * temp2  / temp);
                //System.out.println("Delivery prob : "+deliveryProb);
            }
            write(updateCounter+"\t"+deliveryProb);

            updateCounter++; // new added
            this.lastRecord = simTime - simTime % interval;
        }

    }

    @Override
    public void newMessage(Message m) {

        if (nrofCreated.containsKey(updateCounter)){
            int temp = nrofCreated.get(updateCounter);
            temp++;
            nrofCreated.put(updateCounter, temp);
        }
        else {
            nrofCreated.put(updateCounter, 1);
            nrofDelivered.put(updateCounter, 0);
        }
    }

    @Override
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {

    }

    @Override
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {

    }

    @Override
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {

    }

    @Override
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {

        if (firstDelivery){
            if (nrofDelivered.containsKey(updateCounter)){
                int temp = nrofDelivered.get(updateCounter);
                temp++;
                nrofDelivered.put(updateCounter, temp);
            }
        }
    }

    @Override
    public void done()
    {

        super.done();
    }

}