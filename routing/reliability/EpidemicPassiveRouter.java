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

public class EpidemicPassiveRouter implements RoutingDecisionEngine {
	/** Set contain buffer for receipt */
	private Set<String> receiptBuffer;
	/** Set contain summary vectors */
	private List<String> request;
	/** Report Object */
	private ReceiptStatsReport receiptReport;

	public EpidemicPassiveRouter(Settings s) {

	}

	protected EpidemicPassiveRouter(EpidemicPassiveRouter proto) {
		this.request = new ArrayList<>();
		this.receiptBuffer = new HashSet<>();
		this.receiptReport = new ReceiptStatsReport();
	}

	@Override
	public void connectionUp(DTNHost thisHost, DTNHost peer) {
		/** Get message from this host and peer host  */
		Collection <Message> thisMessages = thisHost.getMessageCollection();
		Collection <Message> peerMessages = peer.getMessageCollection();

		/** Clear summary vector for this connection */
		request.clear();

		/** Check peer and this host summary vector, exchange if not same  */
		if (thisMessages.hashCode() != peerMessages.hashCode()) {

			/** Determine which message is needed for peer host */
			exchangeSummaryVector(peerMessages, thisMessages);
		}
	}
	@Override
	public void connectionDown(DTNHost thisHost, DTNHost peer) {
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
		return m.getTo()==aHost;
	}
	@Override
	public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
		/** Get peer host */
		List<DTNHost> hostRelayM = m.getHops();
		DTNHost peer = hostRelayM.get(hostRelayM.size()-2);

		boolean acknowledged = false;

		/** Get peer router */
		EpidemicPassiveRouter partnerRouter = this.getAnotherRouter(peer);

		/** Check whether message is acknowledged */
		for (String r : receiptBuffer){
			if (r.equals(m.getId())){
				acknowledged = true;
			}
		}

		if (acknowledged){
			/** Tell peer router that message m is acknowledged, transfer receipt */
			partnerRouter.receiptBuffer.add(m.getId());

			/** Delete message from peer buffer */
			peer.deleteMessage(m.getId(),false);

			/** Trigger report */
			receiptReport.receiptTransfered(m.getId());


			receiptReport.messageRemovedByReceipt(m);

			/** Check that peer is receipt target  */
			if (m.getFrom().equals(peer)){

				receiptReport.receiptDelivered(m.getId());
			}
			return false;
		}
		else {

			/** Check whether this message is for this host */
			if (m.getTo()==thisHost){

				/** Create receipt for message m */
				receiptReport.receiptCreated(m);

				/** Add receipt to host and peer buffer */
				receiptBuffer.add(m.getId());
				partnerRouter.receiptBuffer.add(m.getId());
			}

			return true;
		}
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
		return true;
	}


	@Override
	public RoutingDecisionEngine replicate() {
		return new EpidemicPassiveRouter(this);
	}

	private void exchangeSummaryVector(Collection <Message> peerMessages, Collection <Message> thisHostMessages) {
		/** Check for all this host message peer host didn't have, add to summary vector */
		for (Message message : thisHostMessages) {
			if (!peerMessages.contains(message)) {
				request.add(message.getId());
			}
		}
	}

	private EpidemicPassiveRouter getAnotherRouter(DTNHost peer) {
		MessageRouter otherRouter = peer.getRouter();
		assert otherRouter instanceof DecisionEngineRouter : "This router only works with other routers of same type";
		return (EpidemicPassiveRouter) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
	}

	public Set<String> getReceiptBuffer() {
		return receiptBuffer;
	}

}