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
		if(readyProbes == null) return null;
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
		private int newPeriod;
		private GraphDetailFragment graph;
		private int listLength = Const.LIST_LENGTH;
		//TODO
		//Check which container is the fastest
		LinkedList<Measurement> valuesTemperature;
		LinkedList<Measurement> valuesHumidity;

		Probe(String name, String address) {
			this.name = name;
			this.address = address;
			this.setReady(false);
			this.graph = null;
			this.valuesTemperature = new LinkedList<Measurement>();
			this.valuesHumidity = new LinkedList<Measurement>();
			int newPeriod = 1;
		}

		Probe(int id) {
			this.name = null;
			this.address = null;
			this.graph = null;
			this.valuesTemperature = null;
			this.valuesHumidity = null;
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
			graph.refresh();
		}
		
		public void pushValueTemperature(double value){
			Measurement measurement = new Measurement(new Date(), value);
			valuesTemperature.addLast(measurement);
			if(valuesTemperature.size() > listLength){
				valuesTemperature.removeFirst();
			}
			updateGraph();
		}
		
		public void pushValueHumidity(double value){
			Measurement measurement = new Measurement(new Date(), value);
			valuesHumidity.addLast(measurement);
			if(valuesHumidity.size() > listLength){
				valuesHumidity.removeFirst();
			}
			updateGraph();
		}
		
		public LinkedList<Measurement> getValuesTemperature(){
			return valuesTemperature;
		}
		
		public LinkedList<Measurement> getValuesHumidity(){
			return valuesHumidity;
		}
		
		public int getListLength(){
			return listLength;
		}
		
		public void startInjectingValues(){
			activate();
			mainActivity.requestProbeSession(address);
		}

		public synchronized int getNewPeriod() {
			return newPeriod;
		}

		public synchronized void setNewPeriod(int newPeriod) {
			this.newPeriod = newPeriod;
		}
	}
}
