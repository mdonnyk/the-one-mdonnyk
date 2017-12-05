package routing.reliability;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import report.ReceiptStatsReport;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import java.util.*;

/**
 *
 * @Author Michael Donny Kusuma
 * Universitas Sanata Dharma
 */

public class EpidemicActiveRouter implements RoutingDecisionEngine {
    /** buffer that save receipt */
    private Map<String, DTNHost> receiptBuffer;
    /** request message to send to other node if any connection occur */
    private List<String> request;
    /** message that should be deleted */
    private Set<String> messageReadytoDelete;
    /** Report Object */
    private ReceiptStatsReport receiptReport;

    public EpidemicActiveRouter(Settings s) {

    }

    protected EpidemicActiveRouter(EpidemicActiveRouter proto) {
        this.receiptBuffer = new HashMap<>();
        this.receiptReport = new ReceiptStatsReport();
        this.request = new ArrayList<>();
        this.messageReadytoDelete = new HashSet<>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        EpidemicActiveRouter peerRouter = this.getAnotherRouter(peer);

        Collection<Message> thisMessageList = thisHost.getMessageCollection();
        Collection<Message> peerMessageList = peer.getMessageCollection();

        /** Exchange receipt */
        Map<String, DTNHost> peerReceiptBuffer = peerRouter.getReceiptBuffer();

        for (Map.Entry<String, DTNHost> entry : peerReceiptBuffer.entrySet()){
            if (!receiptBuffer.containsKey(entry.getKey())){
                receiptBuffer.put(entry.getKey(), entry.getValue());

                // create report if receipt transfered
               // System.out.println(thisHost.getAddress()+" to "+ peer.getAddress() +" : Receipt "+entry.getKey()+" Transfered");
                receiptReport.receiptTransfered(entry.getKey());
            }
            // Create report if receipt delivered
            if (entry.getValue().equals(thisHost)){
                receiptReport.receiptDelivered(entry.getKey());
            }
        }

        for (Message m : thisMessageList){
            /** Delete message that have a receipt */
            if (receiptBuffer.containsKey(m.getId())){
                // Create report if message removed by receipt
                //System.out.println(thisHost.getAddress()+" to "+ peer.getAddress() +" : Receipt "+m.getId()+" Deleted");
                receiptReport.messageRemovedByReceipt(m);
                messageReadytoDelete.add(m.getId());
            }
            else {
                /** Create a requested message list based from this host message that not have receipt */
                request.add(m.getId());
            }
        }

        /** Remove message that peer is already have */
        for (Message m : peerMessageList){
            if (request.contains(m.getId())){
                request.remove(m.getId());
            }
        }
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        /** Clear requested message list and message to delete list for new connection */
        request.clear();
        messageReadytoDelete.clear();

    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {

    }

    @Override
    public boolean newMessage(Message m) {
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo().equals(aHost);
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        if (isFinalDest(m, thisHost) && !receiptBuffer.containsKey(m.getId() )){
            // Report if receipt created
            receiptReport.receiptCreated(m);
            receiptBuffer.put(m.getId(), m.getFrom());
        }
        return true;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        return request.contains(m.getId());
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return false;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new EpidemicActiveRouter(this);
    }

    @Override
    public boolean shouldDeleteMessage(Message m) {
        return messageReadytoDelete.contains(m.getId());
    }

    private EpidemicActiveRouter getAnotherRouter(DTNHost peer) {
        MessageRouter otherRouter = peer.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works with other routers of same type";
        return (EpidemicActiveRouter) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    public Map<String, DTNHost> getReceiptBuffer() {
        return receiptBuffer;
    }

}
