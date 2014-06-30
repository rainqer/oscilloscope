package com.jangonera.oscilloscope;

import java.util.HashMap;

import android.content.Intent;
import android.os.TransactionTooLargeException;
import android.util.Log;

public class ByteGluer {

	public static int range = 65535;
	public static int kelvinZero = -273;
	public static double maximumVoltage = 3.19;
	public static double reverseVoltageDivider = 2;
	public static double voltageLost = 0.07;
	
	private HashMap<String, FourByteMeasurement> mapOfFourByteMeasurements;
	private static ByteGluer instance = null;
	public static ByteGluer getInstance() {
		if(instance == null) instance = new ByteGluer();
		return instance;
	}
	
	private ByteGluer() {
		mapOfFourByteMeasurements = new HashMap<String, FourByteMeasurement>();
	}

	public boolean processNewByte(String address, int byteOfInfo) {

		//check if we have the first byte
		FourByteMeasurement measurement = mapOfFourByteMeasurements.get(address);
		//no byte for this address yet
		if(measurement == null) mapOfFourByteMeasurements.put(address, new FourByteMeasurement(byteOfInfo));
		
		//there is a measurement from this address
		else {
				//mapOfFourByteMeasurements.remove(address);
				//if(updateProbeValues(address, measurement.getTemperature())) Log.i("JGN", "Probe graph updated");
				//else Log.i("JGN", "Updating probe malfunction - unknown address");
				measurement.putNextByte(byteOfInfo);
				if(measurement.isComplete()) return true;
			//int gluedValue = (firstByte << 8) | byteOfInfo;
		}
		return false;
	}
	
	public double getTemperature(String address) {
		return translateToTemperature(mapOfFourByteMeasurements.get(address).getTemperature());
	}
	
	public double getHumidity(String address) {
		return translateToHumidity(mapOfFourByteMeasurements.get(address).getHumidity());
	}
	
	public void RemoveUsedData(String address) {
		mapOfFourByteMeasurements.remove(address);
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
	
	public double translateToHumidity(int data) {
		return -2.0468 + 0.0367*data -0.0000015955 * data * data;
	}
	
	private static class FourByteMeasurement {
		private Integer byte1;
		private Integer byte2;
		private Integer byte3;
		private Integer byte4;
		
		public FourByteMeasurement() {
			byte1 = null;
			byte2 = null;
			byte3 = null;
			byte4 = null;
		}
		
		public FourByteMeasurement(int firstByte) {
			byte1 = firstByte;
			byte2 = null;
			byte3 = null;
			byte4 = null;
		}
		
		public boolean isComplete() {
			if(byte1 == null || byte2 == null || byte3 == null || byte4 == null) return false;
			return true;
		}
		
		public void putNextByte(int byteOfData) {
			if(byte1 == null) byte1 = byteOfData;
			else if (byte2 == null) byte2 = byteOfData;
			else if (byte3 == null) byte3 = byteOfData;
			else byte4 = byteOfData;
		}
		
		public int getTemperature() {
			int gluedValue = (byte1 << 8) | byte2;
			return gluedValue;
		}
		
		public int getHumidity() {
			int gluedValue = (byte3 << 8) | byte4;
			return gluedValue;
		}
	}
}
