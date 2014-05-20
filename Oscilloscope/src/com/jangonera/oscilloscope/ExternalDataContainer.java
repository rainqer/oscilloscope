package com.jangonera.oscilloscope;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import android.util.Log;

public class ExternalDataContainer {
	// Contains a list of scanned-probes and ready-probes. A scanned probe may
	// be
	// added to ready probes. A ready probe is used to create a graph in graphs
	// fragment.
	private ArrayList<Probe> scanProbes;
	private ArrayList<Probe> readyProbes;
	private MainActivity mainActivity;

	// SINGLETON//////////////////////////////////
	private static ExternalDataContainer mInstance = null;

	public static ExternalDataContainer getContainer() {
		if (mInstance == null)
			mInstance = new ExternalDataContainer();
		return mInstance;
	}

	private ExternalDataContainer() {
		scanProbes = new ArrayList<ExternalDataContainer.Probe>();
		readyProbes = new ArrayList<ExternalDataContainer.Probe>();
	}

	// ////////////////////////////////////////////


	public void registerContext(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}
	
	// Scan Probes////////////////////////////////////
	// ///////////////////////////////////////////////
	public Probe getScanProbe(int index) {
		return scanProbes.get(index);
	}

	public int scanDeviceQuantity() {
		return scanProbes.size();
	}
	
	public boolean hasNothingToDisplay(){
		return readyProbes.isEmpty();
	}

	public void addScanDevice(String name, String address) {
		Log.i(Const.tag_EDC, "Adding new device to the list" + name);
		Probe newProbe = new Probe(name, address);
		if(scanProbes.contains(newProbe)){
			Log.i(Const.tag_EDC, name + " - device is on the list");
			return;
		}
		if (addedToReadyProbes(newProbe.address))
			newProbe.setReady(true);
		scanProbes.add(newProbe);

	}

	public void cleanScanList() {
		Log.i(Const.tag_EDC, "Cleaning list");
		scanProbes.clear();
	}

	private boolean addScanProbeToReadyProbes(Probe probe) {
		Probe newProbe = new Probe(probe.name, probe.address);
		if (readyProbes.contains(newProbe))
			return false;
		readyProbes.add(newProbe);
		probe.setReady(true);
		
		//
		newProbe.startInjectingValues();
		return true;
	}

	public void setScanProbeOfAddressAsNoReady(String address) {
		for (Probe probe : scanProbes) {
			if (probe.getAddress().equals(address))
				probe.setReady(false);
		}
	}

	// /////////////////////////////////////////////////
	// /////////////////////////////////////////////////

	// ReadyProbes//////////////////////////////////////
	// /////////////////////////////////////////////////
	public Probe getReadyProbe(int index) {
		if(index>=readyProbes.size()) return null;
		return readyProbes.get(index);
	}

	public int readyDeviceQuantity() {
		return readyProbes.size();
	}

//	public boolean removeReadyProbe(String name, String address) {
//		// create a pseudo Probe of the given props and pass it to the array for
//		// removal
//		if (readyProbes.remove(new Probe(name, address))) {
//			setScanProbeOfAddressAsNoReady(address);
//			return true;
//		}
//		return false;
//	}

	public boolean removeReadyProbe(int index) {
		// create a pseudo Probe of the given props and pass it to the array for
		// removal
		Probe probe = readyProbes.get(index);
		if (readyProbes.remove(probe)) {
			probe.disactivate();
			setScanProbeOfAddressAsNoReady(probe.getAddress());
			mainActivity.finishProbeSession(probe.getAddress());
			return true;
		}
		return false;
	}

	public boolean removeReadyProbe(Probe probe) {
		if (readyProbes.remove(probe)) {
			probe.disactivate();
			setScanProbeOfAddressAsNoReady(probe.getAddress());
			return true;
		}
		return false;
	}

	public boolean addedToReadyProbes(String address) {
		for (Probe probe : readyProbes) {
			if (probe.getAddress().equals(address))
				return true;
		}
		return false;
	}
	
	public Probe getReadyProbeByAddress(String address) {
		for(Probe probe : readyProbes) {
			if(probe.getAddress().equals(address)) return probe;
		}
		return null;
	}

	// /////////////////////////////////////////////////
	// /////////////////////////////////////////////////

	public class Probe {
		private String name;
		private String address;
		private boolean ready;
		private boolean active;
//		private Graph graph;
		private GraphDetailFragment graph;
		private int listLength = Const.LIST_LENGTH;
		//TODO
		//Check which container is the fastest
		LinkedList<Measurement> values;

		Probe(String name, String address) {
			this.name = name;
			this.address = address;
			this.setReady(false);
			this.graph = null;
			// if(addedToReadyProbes(this.address)) this.setReady(true);
			this.values = new LinkedList<Measurement>();
		}

		Probe(int id) {
			this.name = null;
			this.address = null;
			this.graph = null;
			this.values = null;
		}

		public String getName() {
			return name;
		}

		public String getAddress() {
			return address;
		}

		public boolean isAddedToReadyProbes() {
			return ready;
		}

		public void setReady(boolean ready) {
			this.ready = ready;
		}
		
		public void activate() {
			active = true;
		}
		
		public void disactivate() {
			active = false;
		}
		
		public boolean isActive(){
			return active;
		}
		
		@Override
		public boolean equals(Object o) {
			try {
				Probe probe = (Probe) o;
				if (probe.getAddress().equals(address))
					return true;
			} catch (ClassCastException e) {
				Log.v(Const.tag_EDC, "bad class cast");
			} catch (NullPointerException e) {
				Log.v(Const.tag_EDC, "null pointer passed");
			}
			return false;
		}

		// TODO
		// overwrite hash function

		public boolean addToReadyProbes() {
			return addScanProbeToReadyProbes(this);
		}

		public boolean removeFromReadyProbes() {
			return removeReadyProbe(this);
		}

//		public void registerGraph(Graph graph) {
//			this.graph = graph;
//		}
		public void registerGraph(GraphDetailFragment graph) {
		this.graph = graph;
	}

		// every time any information is passed from a socket update is called,
		// if the graph is attached to the probe signal is sent for it to
		// refresh it self
		private void updateGraph() {
			if (graph == null)
				return;
			// it is called from a read thread so post invalidate is neccessary
			// to send invalidate from ui thread
//			graph.postInvalidate();
			graph.refresh();
		}
		
		public void pushValue(int value){
			Measurement measurement = new Measurement(new Date(), value);
			values.addLast(measurement);
			if(values.size() > listLength){
				values.removeFirst();
			}
			updateGraph();
		}
		
		public LinkedList<Measurement> getValues(){
			return values;
		}
		public int getListLength(){
			return listLength;
		}
		
		public void startInjectingValues(){
			activate();
			mainActivity.requestProbeSession(address);
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					int i = 1;
					while(i<100) {
						pushValue(++i);
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
//					BluetoothManager.getBluetoothManager().connectToAProbe(address);
				}
//				pushValue(value);
			}).start();
		}
	}
}
