package com.jangonera.oscilloscope;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class ExternalDataService extends Service{
	//private boolean initilised;
	
	//public BluetoothManager registerContext(MainActivity mainActivity) {
	//	return null;
	//}
	
	//BINDING BLOCK//////////////////////////////////////////////////////
	private final IBinder serviceBinder = new ExternalDataServiceBinder();
	/////////
	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}
	/////////
	public class ExternalDataServiceBinder extends Binder{
		public ExternalDataService getService(){
			return ExternalDataService.this;
		}
	}
	/////////////////////////////////////////////////////////////////////
	

}
