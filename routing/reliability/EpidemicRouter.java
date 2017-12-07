package routing.reliability;

import core.DTNHost;
import core.Message;
import java.util.*;

/**
 *
 * @Author Michael Donny Kusuma
 * Sanata Dharma Univeristy
 */

public class EpidemicRouter implements routing.RoutingDecisionEngine {
    /** request message to send to other node if any connection occur */
    private Set<String> request;
  
    public EpidemicRouter(core.Settings s) {}
  
    protected EpidemicRouter(EpidemicRouter proto) {
        request = new HashSet<>();
    }

    public void connectionUp(DTNHost thisHost, DTNHost peer) {


        Collection<Message> thisHostMessages = thisHost.getMessageCollection();
        Collection<Message> peerMessages = peer.getMessageCollection();

        /**for every message that this node have but peer node doesn't
         * add to request list */
        for (Message m : thisHostMessages){
            if (!peerMessages.contains(m)){
                request.add(m.getId());
            }
        }
    }

    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        /** clear request message to use for a new connection */
        request.clear();
    }
  
    public void doExchangeForNewConnection(core.Connection con, DTNHost peer) {

  }

    public boolean newMessage(Message m) {
        return true;
    }

    public boolean isFinalDest(Message m, DTNHost aHost) {
    return m.getTo() == aHost;
  }
  
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
    return true;
  }
  
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        /** send request messages to other node */
      return request.contains(m.getId());
  }
  
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
    return false;
  }
  
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
    return false;
  }

    public routing.RoutingDecisionEngine replicate() { return new EpidemicRouter(this);

    }

}