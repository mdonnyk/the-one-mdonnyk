/*
package report;

import core.*;
import java.util.*;

public class KeyConvergenceReport extends Report implements MessageListener {

    private Map <Message, Double> messageConvergenceFinal;
    private Map <Message, List<Double>> messageConvergence;

    public KeyConvergenceReport() {
        super.init();
        messageConvergence = new TreeMap<>();
        messageConvergenceFinal = new TreeMap<>();
    }

    @Override public void newMessage(Message m) { }
    @Override public void messageTransferStarted(Message m, DTNHost from, DTNHost to) { }
    @Override public void messageDeleted(Message m, DTNHost where, boolean dropped) { }
    @Override public void messageTransferAborted(Message m, DTNHost from, DTNHost to) { }

    @Override
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
        if (!messageConvergence.containsKey(m)) messageConvergence.put(m, new ArrayList<>());
        messageConvergence.get(m).add(m.getReceiveTime() - m.getCreationTime());
    }

    @Override
    public void done() {
        for (Map.Entry<Message, List<Double>> messageKey : messageConvergence.entrySet()) {
            double temp = 0; // put average value for each message to a new map
            for (Double convergenceTime : messageKey.getValue()) { temp += convergenceTime; }
            messageConvergenceFinal.put(messageKey.getKey(), temp/messageConvergence.size());
        }

        double temp = 0;
        for (Map.Entry <Message, Double> convergenceReport : messageConvergenceFinal.entrySet()) {
            temp += convergenceReport.getValue(); // total all averaged convergence time
            write(convergenceReport.getKey().toString() + " : " + convergenceReport.getValue());
        }

        // print the average convergence key that exist in the network
        write("\nAverage Key Convergence : " + temp/messageConvergenceFinal.size());
        super.done();

    }
}*/
