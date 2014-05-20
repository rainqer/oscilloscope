package com.jangonera.oscilloscope;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.oscilloscope.R;

public class MainActivity extends ActionBarActivity {
	public static int OPEN = 101;
	public static int CLOSED = 102;
    public static long DRAWER_DELAY = 1000;
	private SetupFragment setupFRAG;
	private GraphsFragment graphsFRAG;
	private GraphDetailFragment graphDetailsFRAG;
	//private boolean smallScreen;
	private BluetoothManager bluetoothManager;
	private ExternalServiceDataReceiver interpreter;
	private ExternalDataService myService;
	private ExternalDataContainer externalDataContainer;
	// use mBound to check if the service is available
//	private boolean mBound;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    public static int drawerDelay = 500;
    private static final int mainScreen = -1;
    private static int screen = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Remember whether we were scanning or not
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		externalDataContainer = ExternalDataContainer.getContainer();
		externalDataContainer.registerContext(this);

        bluetoothManager = BluetoothManager.getBluetoothManager();
        interpreter = new ExternalServiceDataReceiver();
		if (savedInstanceState != null && savedInstanceState.getInt("graph") != mainScreen && loadGraphDetails(savedInstanceState.getInt("graph")));
		else loadGraphs();
        loadSetup();
        loadDrawer();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("graph", screen);
	}

	@Override
	protected void onStart() {
		super.onStart();
		registerReceivers(bluetoothManager, interpreter);
		connectToService();
		checkIfLockDrawerOpen();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(bluetoothManager);
		if(mDrawerLayout != null) mDrawerLayout.closeDrawer(Gravity.LEFT);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	// Scan for probes button in setup
	public void scanForDevices(View view) {
//		if (isBound()) {
		bluetoothManager.scanForDevices();
		invalidateScannedDeviceList();
		markAsDiscovering();
//		}
	}

	public ExternalDataService getService() {
		return myService;
	}

//	public boolean isBound() {
//		return mBound;
//	}
	
	public void checkIfLockDrawerOpen(){
		if(graphsFRAG!=null && graphsFRAG.hasNothingToDisplay()){
			mDrawerLayout.postDelayed(new Runnable() {
				@Override
				public void run() {
					if(mDrawerLayout!=null) mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
			        getSupportActionBar().setHomeButtonEnabled(false);
				}
			}, drawerDelay);
		}
		else{
			if(mDrawerLayout!= null) mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
	        getSupportActionBar().setHomeButtonEnabled(true);
		}
	}

	public void invalidateScannedDeviceList() {
		if (setupFRAG != null)
			setupFRAG.invalidateList();
	}
	
	public void invalidateGraphsList() {
		if (graphsFRAG != null){
			graphsFRAG.invalidateList();
		}
	}

	public void loadSetup() {
		if(setupFRAG == null) setupFRAG = new SetupFragment();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.area_setup, setupFRAG);	
		ft.commit();
	}

	public void addGraph(int index) {
		if(externalDataContainer.getScanProbe(index).addToReadyProbes()){
			display(Const.NEW_GRAPH_ADDED);
			//loadGraphs();
			invalidateGraphsList();
			//refresh to display which probe is "displaying"
			invalidateScannedDeviceList();
		}
		//else loadGraphs();
		checkIfLockDrawerOpen();
	}
	
	public void removeGraph(int index){
		if(externalDataContainer.removeReadyProbe(index)){
			display(Const.GRAPH_REMOVED);
			//loadGraphs();
			invalidateGraphsList();
			//refresh to display which probe is "displaying"
			invalidateScannedDeviceList();
			checkIfLockDrawerOpen();
		}
	}

	public void loadGraphs() {
		if(graphsFRAG == null) graphsFRAG = new GraphsFragment();
		screen = mainScreen;
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.area_graphs, graphsFRAG);
		ft.commit();
	}
	
	public boolean loadGraphDetails(int index) {
		if(externalDataContainer.getReadyProbe(index) == null) return false;
		screen = index;
		graphDetailsFRAG = new GraphDetailFragment();
		graphDetailsFRAG.registerProbe(externalDataContainer.getReadyProbe(index));
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.area_graphs, graphDetailsFRAG);
		ft.commit();
		return true;
	}
	
	public void loadGraphDetailsWithAnimation(int index) {
		if(externalDataContainer.getReadyProbe(index) == null) return;
		screen = index;
		graphDetailsFRAG = new GraphDetailFragment();
		graphDetailsFRAG.registerProbe(externalDataContainer.getReadyProbe(index));
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.side_in_right, R.anim.side_out_left);
		ft.replace(R.id.area_graphs, graphDetailsFRAG);
		ft.commit();
	}
	
	public void hideGraphDetails() {
		if(graphsFRAG == null) graphsFRAG = new GraphsFragment();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.side_in_left, R.anim.side_out_right);
		ft.replace(R.id.area_graphs, graphsFRAG);
		ft.commit();
	}
	
	public void openDrawer() {
		if(mDrawerLayout != null) mDrawerLayout.openDrawer(Gravity.LEFT);
    }
    
	private void registerReceivers(BluetoothManager bluetoothManager, ExternalServiceDataReceiver interpreter) {
		bluetoothManager.registerContext(this);
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(bluetoothManager, filter);
		
		interpreter.registerContext(this);
		filter = new IntentFilter(ExternalServiceDataReceiver.SERVICE_READY);
		filter.addAction(ExternalServiceDataReceiver.SERVICE_DATA_UPDATE);
		registerReceiver(interpreter, filter);
	}
	
	private void loadDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(mDrawerLayout == null) return;
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, OPEN, CLOSED){
        	@Override
        	public void onDrawerOpened(View drawerView) {
        		super.onDrawerOpened(drawerView);
        		getSupportActionBar().setTitle(getString(R.string.title_activity_setup_fragment));
        	}
        	@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
        		getSupportActionBar().setTitle(getString(R.string.app_name));
			}
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	// DATA EXCHANGE///////////////////////////////
	private void connectToService() {
		Intent intent = new Intent(this, ExternalDataService.class);
		startService(intent);
	}
	
	public void requestProbeSession(String address) {
		Intent intent = new Intent(this, ExternalDataService.class);
		intent.putExtra(ExternalDataService.PROBE_SESSION_REQUEST, address);
		startService(intent);
	}

	public void finishProbeSession(String address) {
		Intent intent = new Intent(this, ExternalDataService.class);
		intent.putExtra(ExternalDataService.PROBE_SESSION_CANCEL_REQUEST, address);
		startService(intent);
	}

	public void display(int message) {
		switch (message) {
		case Const.BLUETOOTH_UNAVAILABLE:
			Toast.makeText(this, R.string.blueetooth_unavailable,
					Toast.LENGTH_SHORT).show();
			break;
		case Const.BLUETOOTH_NEW_DEVICE:
			Toast.makeText(this, R.string.new_device_found, Toast.LENGTH_SHORT)
					.show();
			break;
		case Const.NEW_GRAPH_ADDED:
			Toast.makeText(this, R.string.new_graph_added, Toast.LENGTH_SHORT)
					.show();
			break;
		case Const.GRAPH_REMOVED:
			Toast.makeText(this, R.string.graph_removed, Toast.LENGTH_SHORT)
					.show();
			break;
		}
	}

	public void markAsDiscovering() {
		if (setupFRAG != null)
			setupFRAG.markAsDiscovering();
	}

	public void markAsFinishedDiscovery() {
		if (setupFRAG != null)
			setupFRAG.markAsFinishedDiscovery();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if ((requestCode == Const.REQUEST_BLUETOOTH_ON)
				&& (resultCode == Activity.RESULT_OK)) {
			scanForDevices(null);
		}

	}

	public BluetoothManager getBluetoothManager() {
		return bluetoothManager;
	}
	
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
          return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
