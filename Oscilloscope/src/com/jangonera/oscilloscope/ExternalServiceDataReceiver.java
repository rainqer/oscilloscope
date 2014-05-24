package com.jangonera.oscilloscope;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ExternalServiceDataReceiver extends BroadcastReceiver {

//	MainActivity mainActivity;
	public static final String SERVICE_READY = "com.jangonera.oscilloscope.ExternalServiceDataReceiver.SERVICE_READY";
	public static final String SERVICE_DATA_UPDATE = "com.jangonera.oscilloscope.ExternalServiceDataReceiver.SERVICE_DATA_UPDATE";
	public static final String SERVICE_REMOVE_PROBE = "com.jangonera.oscilloscope.ExternalServiceDataReceiver.SERVICE_REMOVE_PROBE";
	

	public static final String PROBE_ADDRESS = "address";
	public static final String DATA = "data";
	
	private ExternalDataContainer externalDataContainer;
	
	public ExternalServiceDataReceiver() {
		externalDataContainer = ExternalDataContainer.getContainer();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if(action.equals(SERVICE_DATA_UPDATE)) {
			Log.i("JGN", "RECEIVED - DATA");
			if(updateProbeValues(intent.getExtras().getString(PROBE_ADDRESS), intent.getExtras().getInt(DATA))) Log.i("JGN", "Probe graph updated");
			else Log.i("JGN", "Communication malfunction - unknown address");
		}
		else if(action.equals(SERVICE_REMOVE_PROBE)) {
			Log.i("JGN", "RECEIVED - REMOVE" + intent.getExtras().getString(PROBE_ADDRESS));
			
		}
		else{
			Log.i("JGN", "RECEIVED - SERVICE READY");
		}
	}

//	public void registerContext(MainActivity mainActivity) {
//		this.mainActivity = mainActivity;
//	}
	
	public boolean updateProbeValues(String address, int data) {
		ExternalDataContainer.Probe probe = externalDataContainer.getReadyProbeByAddress(address);
		if(probe == null) return false;
		probe.pushValue(data);
		return true;
	}
}
