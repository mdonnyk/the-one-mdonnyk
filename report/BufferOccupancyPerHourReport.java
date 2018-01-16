package report;

/** 
 * Records the average buffer occupancy and its variance with format:
 * <p>
 * <Simulation time> <average buffer occupancy % [0..100]> <variance>
 * </p>
 * 
 * 
 */
import java.util.*;
import core.DTNHost;
import core.Settings;
import core.SimClock;
import core.UpdateListener;

public class BufferOccupancyPerHourReport extends Report implements UpdateListener {

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

	private Map<DTNHost, ArrayList<Double>> bufferCounts = new HashMap<DTNHost, ArrayList<Double>>();
	private int updateCounter = 0;  //new added
	
	
	public BufferOccupancyPerHourReport() {
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
			//lastRecord = SimClock.getTime();
			printLine(hosts);
			//System.out.println("Simulation time :"+updateCounter+" hours");
			updateCounter++; // new added

			this.lastRecord = simTime - simTime % interval;
		}
	}
	
	/**
	 * Prints a snapshot of the average buffer occupancy
	 * @param hosts The list of hosts in the simulation
	 */
	 
	private void printLine(List<DTNHost> hosts) {

		//test
		int count = 0;
		double jumlah = 0;
		for (DTNHost h : hosts ) {
			count++; // test
			ArrayList<Double> bufferList = new ArrayList<Double>();
			double temp = h.getBufferOccupancy();
			temp = (temp<=100.0)?(temp):(100.0);
			jumlah+=temp;
			if (bufferCounts.containsKey(h)){
				bufferList = bufferCounts.get(h);
				bufferList.add(temp);
				bufferCounts.put(h, bufferList);
			}
			else {
				bufferCounts.put(h, bufferList);
			}
		}

		//System.out.println("AVG buffer = "+(jumlah/count));
	}

	@Override
	public void done()
	{
		
		for (Map.Entry<DTNHost, ArrayList<Double>> entry : bufferCounts.entrySet()) {

			String printHost = "N"+entry.getKey().getAddress();
			for (Double bufferList : entry.getValue()){
				printHost = printHost + "," + bufferList;
			}
			write(printHost);
		}
		super.done();
	}

	
}
