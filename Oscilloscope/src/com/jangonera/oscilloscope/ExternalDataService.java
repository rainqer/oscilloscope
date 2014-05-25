package com.jangonera.oscilloscope;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ExternalDataService extends Service{
	
	public static final String PROBE_SESSION_REQUEST = "com.jangonera.oscilloscope.ExternalDataService.PROBE_SESSION_REQUEST";
	public static final String PROBE_SESSION_CANCEL_REQUEST = "com.jangonera.oscilloscope.ExternalDataService.PROBE_SESSION_CANCEL_REQUEST";
	public static final String PROBE_SETTINGS_REQUEST = "com.jangonera.oscilloscope.ExternalDataService.PROBE_SETTINGS_REQUEST";
	public static final String PROBE_SETTINGS_REQUEST_PERIOD = "com.jangonera.oscilloscope.ExternalDataService.PROBE_SETTINGS_REQUEST_PERIOD";
	
	private int connectedProbes;
	
	private Map<String, SocketReader> addressReaderMap;
	
	private boolean running;
	public boolean isRunning() {
		return running;
	}
	private BluetoothManager bluetoothManager;
	
	private void readProbes() {
		running = true;
		new ServiceLoop().start();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("JGN", "SERVICE IS IN ONSTART");
		if(!isRunning()) readProbes();
		if((intent.getExtras()) != null){
			if(intent.getExtras().get(PROBE_SESSION_REQUEST) != null) new SocketCreator((String) intent.getExtras().get(PROBE_SESSION_REQUEST)).start();
			if(intent.getExtras().get(PROBE_SESSION_CANCEL_REQUEST) != null) cancelProbeSession(intent.getExtras().getString(PROBE_SESSION_CANCEL_REQUEST));
			if(intent.getExtras().get(PROBE_SETTINGS_REQUEST) != null) {
				sendNewPeriodToProbe(intent.getExtras().getString(PROBE_SETTINGS_REQUEST), intent.getExtras().getInt(PROBE_SETTINGS_REQUEST_PERIOD));
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private class ServiceLoop extends Thread {

		@Override
		public void run() {
			while(true) {
				try {
					sleep(60000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(!hasConnectedProbes()) ExternalDataService.this.stopSelf();
				}
		}
	}

	private synchronized void cancelProbeSession(String address) {
		Log.e("JGN", "SERVICE is requested to close probe session: " + address);
		SocketReader socketToClose = addressReaderMap.get(address);
		if(socketToClose!= null) socketToClose.finish();
	}
	
	private synchronized void sendNewPeriodToProbe(String address, int period) {
		Log.e("JGN", "SERVICE is requested to send new settings to probe : " + address);
		SocketReader socketToWrite = addressReaderMap.get(address);
		if(socketToWrite!= null) socketToWrite.writeData(true, period);
	}
	
	private synchronized void startNewSocketReader(String address, BluetoothSocket bSocket) {
		SocketReader socketReader = new SocketReader(address, bSocket);
		addressReaderMap.put(address, socketReader);
		socketReader.start();
	}

	private class SocketCreator extends Thread {
		private String address;
		public SocketCreator(String address) {
			this.address = address;
		}
		
		@Override
		public void run() {
			BluetoothSocket bSocket = bluetoothManager.connectToAddress(address);
			if(bSocket != null){
				addConnectedProbe();
				startNewSocketReader(address, bSocket);
			}
		}
	}
	
	private class SocketReader extends Thread {
		private final int numberOfTries = 120;
		private final int tryPeriod = 1*1000;//2 minute time out
		private final int availaableTimeOuts = 3;
		private int timeoutsUsed;
		private boolean finish;
		private boolean writeData;
		private int newPeriod;
		
		private BluetoothSocket bSocket;
		private String address;
		public SocketReader(String address, BluetoothSocket bSocket){
			this.bSocket = bSocket;
			timeoutsUsed = 0;
			this.address = address;
			finish = false;
		}
		
		public synchronized void finish() {
			finish = true;
		}
		
		public synchronized boolean shouldFinish() {
			return finish;
		}
		
		public synchronized void writeData(boolean writeData, int newPeriod) {
			this.writeData = writeData;
			this.newPeriod = newPeriod;
		}
		
		public synchronized boolean shouldWriteData() {
			return writeData;
		}
		
		public synchronized int getNewPeriod() {
			return newPeriod;
		}
		
		@Override
		public void run() {
			super.run();
	        InputStream inStream = getInputStream();
	        OutputStream outStream = getOutputStream();
	        if(inStream == null) return;
        	int a;
        	int tryNumber = 0;
            while(true){
            	tryNumber = 0;
				timeoutsUsed = 0;
	            try {
	            	while(inStream.available() == 0) {
	            		//Main thread told us to finish the session
	            		if(shouldFinish()) {
	            			inStream.close();
	            			close();
	            			return;
	            		}
	            		//Main thread told us to send new period settings
	            		if(shouldWriteData()) {
	            			writeNewData(outStream, getNewPeriod());
	            		}
	            		if(tryNumber >= numberOfTries) {
	            			//Close the input stream and try to get a new one
	            			inStream.close();
	            			inStream = processTimeOut();
	            			if(inStream == null){
	            				//tell the main thread that we finished the session
	            				removeAConnectedProbe(address);
	            				return;
	            			}
	            			tryNumber = 0;
	            			continue;
	            		}
	            		++tryNumber;
	            		sleep(tryPeriod);
	            	}
					processReceivedData(inStream.read());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
		}

		private void writeNewData(OutputStream outStream, int newPeriod2) throws IOException {
			if(outStream != null) {
				outStream.write(2);
				outStream.write(getNewPeriod());
				outStream.write(3);
			}
			writeData(false, 0);
		}

		private void close() {
			try {
				addressReaderMap.remove(address);
				--connectedProbes;
				bSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.e("JGN", "Service closed session" + address);
		}

		private void processReceivedData(int data) {
			Intent intent = new Intent(ExternalServiceDataReceiver.SERVICE_DATA_UPDATE);
			intent.putExtra(ExternalServiceDataReceiver.PROBE_ADDRESS, address);
			intent.putExtra(ExternalServiceDataReceiver.DATA, data);
			sendBroadcast(intent);
		}

		//Try to reestablish the connection and return new input stream
		private InputStream processTimeOut() {
			//If we used up all the timeouts, decide to close this probe session
			if (timeoutsUsed >= availaableTimeOuts) return null;
			++timeoutsUsed;
			
			try {
				bSocket.close();
				bSocket = bluetoothManager.connectToAddress(address);
				if(bSocket == null) return null;
				return bSocket.getInputStream();
			} catch (IOException e) {
				try {
					bSocket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
				return null;
			}
		}
		
		private InputStream getInputStream() {
			try {
            	return bSocket.getInputStream();
            } 
            catch (IOException e) {
            	return null;
            }
		}
		
		private OutputStream getOutputStream() {
			try {
            	return bSocket.getOutputStream();
            } 
            catch (IOException e) {
            	return null;
            }
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.e("JGN", "SERVICE IS CREATED");
		running = false;
		bluetoothManager = BluetoothManager.getBluetoothManager();
		connectedProbes = 0;
		addressReaderMap = new HashMap<String, ExternalDataService.SocketReader>();
		Intent ready = new Intent(ExternalServiceDataReceiver.SERVICE_READY);
		sendBroadcast(ready);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e("JGN", "SERVICE IS DESTROYED");
		bluetoothManager = null;
	}
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public synchronized boolean hasConnectedProbes() {
		return (connectedProbes > 0);
	}
	public synchronized void addConnectedProbe() {
		++connectedProbes;
	}
	public synchronized void removeAConnectedProbe(String address) {
		Intent intent = new Intent(ExternalServiceDataReceiver.SERVICE_REMOVE_PROBE);
		intent.putExtra(ExternalServiceDataReceiver.PROBE_ADDRESS, address);
		sendBroadcast(intent);
		--connectedProbes;
	}
}
