
/*
 * @antok
 */

package report;


import java.util.HashMap;
import java.util.Map;

import core.DTNHost;
import core.Message;
import core.MessageListener;



public class BufferOverflowReport extends Report implements MessageListener {
	
	/**
	 * Report for generating statistics of number of message drop
	 * due to buffer overflow (creating a new message, receiving relay message
	 * from a peer
	 */
	
	protected Map<DTNHost, Integer> dropCounts;
	
	
	/**
	 * Constructor.
	 */
	public BufferOverflowReport() {
		init();
	}
	
	@Override
	protected void init() {
				
		super.init();
		dropCounts = new HashMap <DTNHost, Integer>();
	}
	
	
	public void messageDeleted(Message m, DTNHost where, boolean dropped) {
		if (isWarmupID(m.getId())) {
			return;
		}
		
		if (dropped) {
			
			if (dropCounts.containsKey(where))
				this.dropCounts.put(where, dropCounts.get(where)+1);
			else
				this.dropCounts.put(where, 1);
						
		}
				
	}
	
	
	public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {}
	
	public void messageTransferred(Message m, DTNHost from, DTNHost to,
			boolean finalTarget) {}
	
	public void newMessage(Message m) {}
	
	public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {}
	
	
	@Override
	public void done() {
		
		for(Map.Entry<DTNHost, Integer> entry : dropCounts.entrySet())
		{
			
			DTNHost a = entry.getKey();
			Integer b = a.getAddress();
						
			write("" + b + ' ' + entry.getValue());
		}
		
		super.done();
	}
		

}
