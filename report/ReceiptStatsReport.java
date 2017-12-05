package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;


import java.util.*;

public class ReceiptStatsReport extends Report {
    /** List of message that acknowledged*//*
    private static Map<String, Double> messageAcknowledged = new HashMap<>();
    *//** Time that message completely removed in network start from receipt created*//*
    private static Map<String, Double> messageRemovedTime = new HashMap<>();*/
    private static Set<String> noReceiptDelivered = new HashSet<String>();

    private static int nrofReceiptCreated = 0;
    private static int nrofReceiptDelivered = 0;
    private static int nrofReceiptRelayed = 0;
    private static int nrofMessageDeletedbyReceipt = 0;

    public void receiptCreated(Message messageCreateReceipt) {
        this.nrofReceiptCreated++;
        /** Update list of message that acknowledged and when that message acknowledged*//*
        messageAcknowledged.put(m.getId(), getSimTime());*/
    }

    public void receiptTransfered(String r){
        this.nrofReceiptRelayed++;
    }

    public void receiptDelivered(String r) {
        noReceiptDelivered.add(r);
        nrofReceiptDelivered++;
    }

    public void messageRemovedByReceipt(Message messageDeletedbyReceipt){
        nrofMessageDeletedbyReceipt++;
        /*double receiptCreatedTime = messageAcknowledged.get(r.getId());
        *//** Update time*//*
        this.messageRemovedTime.put(r.getId(), getSimTime()-receiptCreatedTime);*/
    }


    @Override
    public void done()
    {

        /*List<Double> convergenceTime = new ArrayList<Double>();
        for (Map.Entry<String, Double> receipt : messageRemovedTime.entrySet()) {
            convergenceTime.add(receipt.getValue());
        }*/

        write("Receipt stats for scenario " + getScenarioName() +
                "\nsim_time: " + format(getSimTime()));

        String statsText = "recipt_created: " + this.nrofReceiptCreated +
                "\nmessage_deleted: " + this.nrofMessageDeletedbyReceipt +
                "\nreceipt_delivered1: " + this.noReceiptDelivered.size() +
                "\nreceipt_delivered2: " + this.nrofReceiptDelivered +
                "\nreceipt_relayed: " + this.nrofReceiptRelayed +
                "\nconvergence_time_average: " + "NaN"
                ;

        write(statsText);
        nrofReceiptCreated = 0;
        nrofReceiptDelivered = 0;
        nrofReceiptRelayed = 0;
        nrofMessageDeletedbyReceipt = 0;
        super.done();

    }

}
