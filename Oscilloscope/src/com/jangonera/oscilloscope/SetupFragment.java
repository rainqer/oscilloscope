package com.jangonera.oscilloscope;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.oscilloscope.R;
import com.jangonera.oscilloscope.customview.IconView;

public class SetupFragment extends Fragment {

	private ListView listOfDevices;
	private BaseAdapter listAdapter;
	private MainActivity context;
	private ExternalDataContainer externalDataContainer;
	private TextView setupActionHint;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = (MainActivity) getActivity();
		externalDataContainer = ExternalDataContainer.getContainer();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View content = inflater.inflate(R.layout.fragment_setup, container,
				false);
		return content;
	}

	@Override
	public void onResume() {
		super.onResume();
		loadListOfDevices();
		loadHint(BluetoothManager.getBluetoothManager().isDiscovering());
	}	
	
	public void loadHint(boolean discovering){
		if(discovering) markAsDiscovering();
		else markAsFinishedDiscovery();
	}

	private void loadListOfDevices() {
		listOfDevices = (ListView) context
				.findViewById(R.id.setup_listOfDevices);
		listAdapter = new BaseAdapter() {
			
			@Override
			public View getView(int position, View convertView,
					ViewGroup viewGroup) {
				// If none convert not available, create new
				if (convertView == null) {
					convertView = context.getLayoutInflater().inflate(
							R.layout.listitem_device_setup, viewGroup, false);
					convertView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							context.addGraph(v.getId());
						}
					});
				}
				convertView.setId(position);
				// adjust the convert view
				TextView deviceName = (TextView) convertView
						.findViewById(R.id.device_name);
				deviceName.setText(externalDataContainer.getScanProbe(position)
						.getName());
				//Add Button Listener
				IconView iconView = (IconView) convertView.findViewById(R.id.setup_start_graph);
				TextView questionView = (TextView) convertView.findViewById(R.id.device_added);
				if(externalDataContainer.getScanProbe(position).isAddedToReadyProbes()){
					iconView.setVisibility(View.VISIBLE);
					questionView.setVisibility(View.INVISIBLE);
				}
				else{
					iconView.setVisibility(View.INVISIBLE);
					questionView.setVisibility(View.VISIBLE);
				}
				return convertView;
			}


			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public Object getItem(int position) {
				return externalDataContainer.getScanProbe(position);
			}

			@Override
			public int getCount() {
				return externalDataContainer.scanDeviceQuantity();
			}
		};
		listOfDevices.setAdapter(listAdapter);
	}
	
	public void invalidateList() {
		listAdapter.notifyDataSetChanged();
	}
	
	public void markAsDiscovering() {
		if (setupActionHint == null)
			setupActionHint = (TextView) context.findViewById(R.id.setup_action_hint);
		setupActionHint.setText(R.string.setup_action_scanning);
	}
	
	public void markAsFinishedDiscovery() {
		if (setupActionHint == null)
			setupActionHint = (TextView) context.findViewById(R.id.setup_action_hint);
		setupActionHint.setText(R.string.setup_action_list);
	}
}
