package com.jangonera.oscilloscope;

import com.example.oscilloscope.R;
import com.jangonera.oscilloscope.ExternalDataContainer.Probe;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.TextView;

public class GraphDetailFragment extends Fragment {
	
	private Probe probe;
	
	public void registerProbe(Probe readyProbe) {
		if(probe != null) probe.registerGraph(null);
		probe = readyProbe;
		if(probe != null) probe.registerGraph(this);
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View content = inflater.inflate(R.layout.fragment_graph_details, container,	false);
		return content;
	}

	@Override
	public void onResume() {
		super.onResume();
		((TextView) getActivity().findViewById(R.id.back_button)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				back();
			}
		});
	}
	
	public void back() {
		((MainActivity)getActivity()).hideGraphDetails();
	}
	
	public void refresh() {
	}
}
