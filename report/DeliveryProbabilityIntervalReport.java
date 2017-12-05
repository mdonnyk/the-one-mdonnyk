package report;

import java.util.*;

import core.*;

public class DeliveryProbabilityIntervalReport extends Report implements MessageListener, UpdateListener {


    public static final String BUFFER_REPORT_INTERVAL = "occupancyInterval";
    /** Default value for the snapshot interval */
    public static final int DEFAULT_BUFFER_REPORT_INTERVAL = 3600;
    private double lastRecord = Double.MIN_VALUE;
    private int interval;
    private Map<DTNHost, ArrayList<Double>> bufferCounts = new HashMap<DTNHost, ArrayList<Double>>();
    private int updateCounter = 0;  //new added

    private Map<Integer, Integer> nrofDelivered = new HashMap<Integer, Integer>();
    private Map<Integer, Integer> nrofCreated = new HashMap<Integer, Integer>();
    public DeliveryProbabilityIntervalReport() {
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

            //printLine(hosts);
            System.out.println(updateCounter);
            updateCounter++; // new added
            this.lastRecord = simTime - simTime % interval;
        }
        /**
         for (DTNHost ho : hosts ) {
         double temp = ho.getBufferOccupancy();
         temp = (temp<=100.0)?(temp):(100.0);
         if (bufferCounts.containsKey(ho.getAddress()))
         bufferCounts.put(ho.getAddress(), (bufferCounts.get(ho.getAddress()+temp))/2);
         else
         bufferCounts.put(ho.getAddress(), temp);
         }
         }
         */
    }
    /**
     * Prints a snapshot of the average buffer occupancy
     * @param hosts The list of hosts in the simulation
     */
    private void printLine(List<DTNHost> hosts) {
        /**
         double bufferOccupancy = 0.0;
         double bo2 = 0.0;

         for (DTNHost h : hosts) {
         double tmp = h.getBufferOccupancy();
         tmp = (tmp<=100.0)?(tmp):(100.0);
         bufferOccupancy += tmp;
         bo2 += (tmp*tmp)/100.0;
         }
         double E_X = bufferOccupancy / hosts.size();
         double Var_X = bo2 / hosts.size() - (E_X*E_X)/100.0;

         String output = format(SimClock.getTime()) + " " + format(E_X) + " " +
         format(Var_X);
         write(output);
         */
        for (DTNHost h : hosts ) {
            ArrayList<Double> bufferList = new ArrayList<Double>();
            double temp = h.getBufferOccupancy();
            temp = (temp<=100.0)?(temp):(100.0);
            if (bufferCounts.containsKey(h)){
                //bufferCounts.put(h, (bufferCounts.get(h)+temp)/2); seems WRONG
                //bufferCounts.put(h, bufferCounts.get(h)+temp);
                //write (""+ bufferCounts.get(h));
                bufferList = bufferCounts.get(h);
                bufferList.add(temp);
                bufferCounts.put(h, bufferList);
            }
            else {
                bufferCounts.put(h, bufferList);
                //write (""+ bufferCounts.get(h));
            }
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

        for (int i = 0; i < updateCounter; i++ ){

            double deliveryProb = 0;
            //if (!(this.nrofDelivered.get(updateCounter)==null) || (this.nrofDelivered.get(updateCounter)>0)) {
                System.out.println("Delivered "+nrofDelivered.get(updateCounter));
                deliveryProb = (1.0 * nrofDelivered.get(updateCounter)  / nrofCreated.get(updateCounter));
            //}

            String statsText = i + "\t" +format(deliveryProb);
            write(statsText);

        }
        super.done();
    }
}