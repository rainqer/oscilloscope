package com.jangonera.oscilloscope;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BluetoothManager extends BroadcastReceiver {
	
	private MainActivity context;
	// private ExternalDataService myService;
	private BluetoothAdapter mBluetoothAdapter;

	// SINGLETON//////////////////////////////////
	private static BluetoothManager mInstance = null;

	public static BluetoothManager getBluetoothManager() {
		if (mInstance == null)
			mInstance = new BluetoothManager();
		return mInstance;
	}

	private BluetoothManager() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	public void registerContext(MainActivity con) {
		context = con;
	}

	// ////////////////////////////////////////////

	private void turnTheBluetoothOn() {

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			context.startActivityForResult(enableBtIntent,
					Const.REQUEST_BLUETOOTH_ON);
		}
	}

	// public void registerService(ExternalDataService service) {
	// myService = service;
	// }

	// Clear the whole of devices - then re-scan and store every found device
	// again
	public void scanForDevices() {
		if (mBluetoothAdapter == null) {
			context.display(Const.BLUETOOTH_UNAVAILABLE);
			return;
		}
		// terminate if discovery is already switched on;
		if (mBluetoothAdapter.isDiscovering())
			return;

		turnTheBluetoothOn();
		ExternalDataContainer.getContainer().cleanScanList();
		//context.invalidateList();
		//context.markAsDiscovering();
		Log.i(Const.tag_BM, "Starting discovery now.");
		mBluetoothAdapter.startDiscovery();
	}

	public boolean isDiscovering() {
		return mBluetoothAdapter.isDiscovering();
	}

	@Override
	public void onReceive(Context con, Intent intent) {
		Log.i(Const.tag_BM, intent.getAction());
		if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
			BluetoothDevice device = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			ExternalDataContainer.getContainer().addScanDevice(
					device.getName(), device.getAddress());
			context.invalidateScannedDeviceList();
		} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(intent
				.getAction())) {
		} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent
				.getAction())) {
			context.markAsFinishedDiscovery();
		}
	}
}
