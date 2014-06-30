package com.jangonera.oscilloscope;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ExternalServiceDataReceiver extends BroadcastReceiver {

	public static final String SERVICE_READY = "com.jangonera.oscilloscope.ExternalServiceDataReceiver.SERVICE_READY";
	public static final String SERVICE_DATA_UPDATE = "com.jangonera.oscilloscope.ExternalServiceDataReceiver.SERVICE_DATA_UPDATE";
	public static final String SERVICE_REMOVE_PROBE = "com.jangonera.oscilloscope.ExternalServiceDataReceiver.SERVICE_REMOVE_PROBE";
	

	public static final String PROBE_ADDRESS = "address";
	public static final String DATA_T = "temp";
	public static final String DATA_H = "hum";
		
	private ExternalDataContainer externalDataContainer;
	public ExternalServiceDataReceiver() {
		externalDataContainer = ExternalDataContainer.getContainer();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if(action.equals(SERVICE_DATA_UPDATE)) {
			Log.i("JGN", "RECEIVED - DATA");
			if(updateProbeValues(intent.getExtras().getString(PROBE_ADDRESS), intent.getExtras().getDouble(DATA_T), intent.getExtras().getDouble(DATA_H))) Log.i("JGN", "Probe temp graph updated");
			else Log.i("JGN", "Updating probe malfunction - unknown address");
			//ByteGluer.getInstance().processNewByte(intent.getExtras().getString(PROBE_ADDRESS), intent.getExtras().getInt(DATA));
		}
		else if(action.equals(SERVICE_REMOVE_PROBE)) {
			Log.i("JGN", "RECEIVED - REMOVE" + intent.getExtras().getString(PROBE_ADDRESS));
		}
		else{
			Log.i("JGN", "RECEIVED - SERVICE READY");
		}
	}
	
	public boolean updateProbeValues(String address, double dataTemp, double dataHum) {
		ExternalDataContainer.Probe probe = externalDataContainer.getReadyProbeByAddress(address);
		if(probe == null) return false;
		probe.pushValueTemperature(dataTemp);
		probe.pushValueHumidity(dataHum);
		return true;
	}
}
