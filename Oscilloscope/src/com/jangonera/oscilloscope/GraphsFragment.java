package com.jangonera.oscilloscope;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.oscilloscope.R;
import com.example.oscilloscope.R.string;
import com.jangonera.oscilloscope.ExternalDataContainer.Probe;
import com.jangonera.oscilloscope.customview.IconView;

public class GraphsFragment extends Fragment {
	private MainActivity context;
	private boolean drawing;
	private BaseAdapter listAdapter;
	private ListView listOfGraphs;
    private TextView messageGraphsEmpty;
	private ExternalDataContainer externalDataContainer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = (MainActivity) getActivity();
		setDrawing(false);
		externalDataContainer = ExternalDataContainer.getContainer();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View content = inflater.inflate(R.layout.fragment_graphs, container,
				false);
		return content;
	}

	@Override
	public void onResume() {
		super.onResume();
		loadListOfGraphs();
	}

	private void loadListOfGraphs() {
        messageGraphsEmpty = (TextView) context
                .findViewById(R.id.graphs_message_empty);
		listOfGraphs = (ListView) context
				.findViewById(R.id.graphs_listOfGraphs);
		listAdapter = new BaseAdapter() {

			@Override
			public View getView(int position, View convertView,
					ViewGroup viewGroup) {
				return getGraphView(position, convertView, viewGroup);
			}

			@Override
			public long getItemId(int index) {
				return index;
			}

			@Override
			public Object getItem(int index) {
				return externalDataContainer.getReadyProbe(index);
			}

			@Override
			public int getCount() {
                int quantity = externalDataContainer.readyDeviceQuantity();
                if(messageGraphsEmpty!= null)
                    if(quantity>0) messageGraphsEmpty.setVisibility(View.INVISIBLE);
                    else messageGraphsEmpty.setVisibility(View.VISIBLE);
				return quantity;
			}
		};
		listOfGraphs.setAdapter(listAdapter);
	}

	public void invalidateList() {
		if (listAdapter != null) {
			listAdapter.notifyDataSetChanged();
		}
	}

	public boolean hasNothingToDisplay() {
		return externalDataContainer.hasNothingToDisplay();
	}
	
	public boolean isDrawing() {
		return drawing;
	}

	public void setDrawing(boolean drawing) {
		this.drawing = drawing;
	}

	// Methods related to graphs
	public View getGraphView(int position, View convertView, ViewGroup viewGroup) {
		// If convert not available, create new
		if (convertView == null) {
			convertView = context.getLayoutInflater().inflate(
					R.layout.listitem_graph_graphs, viewGroup, false);
		}

		convertView.setId(position);
		
		Probe readyProbe = externalDataContainer.getReadyProbe(position);
		
		TextView tvName = (TextView) convertView
				.findViewById(R.id.graphs_probe_name);
		tvName.setText(readyProbe.getName());

		TextView tvAddress = (TextView) convertView
				.findViewById(R.id.graphs_probe_address);
		tvAddress.setText(readyProbe
				.getAddress());
		TextView tvState = (TextView) convertView
				.findViewById(R.id.graphs_probe_state);
		if(readyProbe.isActive()) tvState.setText(context.getResources().getString(string.graphs_state_connected));
		else tvState.setText(context.getResources().getString(string.graphs_state_disconnected));
		((IconView) convertView.findViewById(R.id.graphs_close_graph))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						context.removeGraph(((View) v.getParent()).getId());
					}
				});
		((IconView) convertView.findViewById(R.id.graphs_show_graph))
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				context.loadGraphDetailsWithAnimation(((View) v.getParent()).getId());
			}
		});
		ArrayAdapter adapter = ArrayAdapter.createFromResource(context, R.array.periods, R.layout.custom_spinner);
		adapter.setDropDownViewResource(R.layout.custom_spinner_item);
		Spinner spinner = (Spinner) convertView.findViewById(R.id.graph_period_spinner);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View v,
					int index, long arg3) {
				int newPeriod = 1;
				if(index == 1) newPeriod = 3;
				if(index == 2) newPeriod = 6;
				Probe probe = ExternalDataContainer.getContainer().getReadyProbe(((View) v.getParent().getParent()).getId());
				if(probe != null) probe.setNewPeriod(newPeriod);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		((IconView) convertView.findViewById(R.id.graphs_update_button))
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Probe probeToUpdate = ExternalDataContainer.getContainer().getReadyProbe(((View) v.getParent()).getId());
				if(probeToUpdate!= null ) context.changeProbeSettings(probeToUpdate.getAddress(), probeToUpdate.getNewPeriod());
			}
		});
		return convertView;
	}
}
