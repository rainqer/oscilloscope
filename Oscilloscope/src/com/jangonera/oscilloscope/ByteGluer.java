package com.jangonera.oscilloscope;

import java.util.HashMap;

import android.util.Log;

public class ByteGluer {

	private ExternalDataContainer externalDataContainer;
	private HashMap<String, Integer> mapOfFirstBytes;
	private static ByteGluer instance = null;
	public static ByteGluer getInstance() {
		if(instance == null) instance = new ByteGluer();
		return instance;
	}
	
	private ByteGluer() {
		mapOfFirstBytes = new HashMap<String, Integer>();
		externalDataContainer = ExternalDataContainer.getContainer();
	}

	public void processNewByte(String address, int byteOfInfo) {

		//check if we have the first byte
		Integer firstByte = mapOfFirstBytes.get(address);
		//no byte for this address yet
		if(firstByte == null) mapOfFirstBytes.put(address, Integer.valueOf(byteOfInfo));
		
		//there is a byte from this address
		else {
			mapOfFirstBytes.remove(address);
			int gluedValue = (firstByte << 8) | byteOfInfo;
			if(updateProbeValues(address, gluedValue)) Log.i("JGN", "Probe graph updated");
			else Log.i("JGN", "Updating probe malfunction - unknown address");
		}
	}
	
	public boolean updateProbeValues(String address, int data) {
		ExternalDataContainer.Probe probe = externalDataContainer.getReadyProbeByAddress(address);
		if(probe == null) return false;
		probe.pushValue(data);
		return true;
	}
}
