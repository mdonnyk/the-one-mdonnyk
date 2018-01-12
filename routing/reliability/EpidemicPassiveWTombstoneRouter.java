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

public class EpidemicPassiveWTombstoneRouter implements RoutingDecisionEngine {
	/** Buffer for receipt */
	private Set<String> receiptBuffer;
	/** Message ever received */
	private Set<String> tombstone;
	/** Peer's request to this node  */
	private List<String> request;
	/** Report */
	private ReceiptStatsReport receiptReport;

	public EpidemicPassiveWTombstoneRouter(Settings s) {

	}

	protected EpidemicPassiveWTombstoneRouter(EpidemicPassiveWTombstoneRouter proto) {
		this.request = new ArrayList<>();
		this.receiptBuffer = new HashSet<>();
		this.tombstone = new HashSet<>();
		this.receiptReport = new ReceiptStatsReport();
	}

	@Override
	public void connectionUp(DTNHost thisHost, DTNHost peer) {
		/** Get message from this host and peer host  */
		Collection <Message> thisMessages = thisHost.getMessageCollection();
		Collection <Message> peerMessages = peer.getMessageCollection();

		/** Get peer's tombstone */
		EpidemicPassiveWTombstoneRouter partnerRouter = this.getAnotherRouter(peer);
		Set<String> peerTombstone = partnerRouter.getTombstone();

		/** Clean up request for this connection */
		request.clear();

		/** Check peer and this host summary vector, exchange if not same  */
		if (thisMessages.hashCode() != peerMessages.hashCode()) {

			/** Determine which message is needed for peer */
			exchangeSummaryVector(peerMessages, thisMessages, peerTombstone);
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

		/** Get peer router */
		EpidemicPassiveWTombstoneRouter partnerRouter = this.getAnotherRouter(peer);

		/** Check whether message is acknowledged */
		/*boolean acknowledged = false;
		for (String r : receiptBuffer){
			if (r.equals(m.getId())){
				acknowledged = true;
			}
		}*/

		if (receiptBuffer.contains(m.getId())){
			/** Tell peer router that message m is acknowledged, transfer receipt */
			partnerRouter.receiptBuffer.add(m.getId());

			/** Delete message from peer buffer */
			peer.deleteMessage(m.getId(),false);

			/** Trigger report */
			//////////
			receiptReport.receiptTransfered(m.getId());
			receiptReport.messageRemovedByReceipt(m);
			//////////

			/** Check that peer is receipt target  */
			if (m.getFrom().equals(peer)){
				//////////
				receiptReport.receiptDelivered(m.getId());
				//////////
			}
			return false;
		}
		else {

			/** Check whether this message is for this host */
			if (m.getTo()==thisHost){

				/** Create receipt for message m */
				//////////
				receiptReport.receiptCreated(m);
				//////////

				/** Add receipt to host and peer buffer */
				receiptBuffer.add(m.getId());
				partnerRouter.receiptBuffer.add(m.getId());
			}

			tombstone.add(m.getId());
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
		return new EpidemicPassiveWTombstoneRouter(this);
	}

	private void exchangeSummaryVector(Collection <Message> peerMessages, Collection <Message> thisHostMessages, Set<String> peerTombstone) {
		/** Check every message that peer needed */
		for (Message message : thisHostMessages) {
			if (!peerMessages.contains(message) && !peerTombstone.contains(message.getId())) {
				request.add(message.getId());
			}
		}
	}

	private EpidemicPassiveWTombstoneRouter getAnotherRouter(DTNHost peer) {
		MessageRouter otherRouter = peer.getRouter();
		assert otherRouter instanceof DecisionEngineRouter : "This router only works with other routers of same type";
		return (EpidemicPassiveWTombstoneRouter) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
	}

	public Set<String> getReceiptBuffer() {
		return receiptBuffer;
	}

	public Set<String> getTombstone() {
		return tombstone;
	}
}