package com.jangonera.oscilloscope;

import java.util.HashMap;

import android.os.TransactionTooLargeException;
import android.util.Log;

public class ByteGluer {

	public static int range = 65535;
	public static int kelvinZero = -273;
	public static double maximumVoltage = 3.19;
	public static double reverseVoltageDivider = 2;
	public static double voltageLost = 0.07;
	
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
		probe.pushValue(translateToTemperature(data));
		return true;
	}
	
	private double translateToTemperature(int data) {
//		double ratio = (double)data / range;
//		double voltage = ratio * maximumVoltage;
//		double voltageBeforeDivider = voltage * reverseVoltageDivider + voltageLost;
//		double measuredKelvin = voltageBeforeDivider * 100;
//		double measuredCelcius = measuredKelvin + kelvinZero;
//		return measuredCelcius;
		return (((((((double)data / range) * maximumVoltage) * reverseVoltageDivider + voltageLost) * 100) + kelvinZero));
	}
}
