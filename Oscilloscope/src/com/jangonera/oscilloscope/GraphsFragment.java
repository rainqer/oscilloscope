package com.jangonera.oscilloscope;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.example.oscilloscope.R;
import com.jangonera.oscilloscope.ExternalDataContainer.Probe;

public class GraphsFragment extends Fragment {
	// Contains a list of graphs. A graphs is used by a probe to draw on it.
	private MainActivity context;
	// private ArrayList<Graph> graphs;
	private boolean drawing;
	private boolean backButtonRequired;
	private BaseAdapter listAdapter;
	private ListView listOfGraphs;
	private ExternalDataContainer externalDataContainer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = (MainActivity) getActivity();
		externalDataContainer = ExternalDataContainer.getContainer();
		setDrawing(false);
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
		if (backButtonRequired)
			createBackButton();
		loadListOfGraphs();

	}

	private void loadListOfGraphs() {
		listOfGraphs = (ListView) context
				.findViewById(R.id.graphs_listOfGraphs);
		listAdapter = new BaseAdapter() {

			@Override
			public View getView(int position, View convertView,
					ViewGroup viewGroup) {

				// return convertView;
				// return graphs.get(position).getView(convertView, viewGroup);
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
				return externalDataContainer.readyDeviceQuantity();
			}
		};
		listOfGraphs.setAdapter(listAdapter);
	}

	public void invalidateList() {
		if (listAdapter != null) {
			listAdapter.notifyDataSetChanged();
		}
	}

	private void createBackButton() {
		TextView backButton = (TextView) context
				.findViewById(R.id.graphs_back_button);
		backButton.setText(R.string.graphs_back);
		backButton.setBackgroundColor(getResources().getColor(
				android.R.color.darker_gray));
		backButton.setTextSize(22);
		backButton.setClickable(true);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				context.loadSetup();
			}
		});
	}

	public boolean isDrawing() {
		return drawing;
	}

	public void setDrawing(boolean drawing) {
		this.drawing = drawing;
	}

	public boolean isBackButtonRequired() {
		return backButtonRequired;
	}

	public void setBackButtonRequired(boolean backButtonRequired) {
		this.backButtonRequired = backButtonRequired;
	}

	// Methods related to graphs
	public View getGraphView(int position, View convertView, ViewGroup viewGroup) {
		// If none convert not available, create new
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
		((ImageButton) convertView.findViewById(R.id.graphs_close_graph))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						context.removeGraph(((View) v.getParent()).getId());
					}
				});

		// Add graph to the view. Graph is taken from the arraylist within
		// external data container
		Graph graph = new Graph(context);
		graph.registerProbe(readyProbe);
		
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.BELOW, R.id.graphs_probe_address);
		params.setMargins(10, 10, 10, 10);
		

		params.addRule(RelativeLayout.BELOW, R.id.graphs_probe_address);
		((ViewGroup) convertView).addView(graph, params);
		// Log.i(Const.tag_GF, "graphs added");
		return convertView;
	}

	// Graph represents the area on which a ready-probe can draw information
	// public class Graph extends View{
	// private Paint paint;
	//
	// public Graph(Context context, AttributeSet attrs, int defStyle) {
	// super(context, attrs, defStyle);
	// }
	// public Graph(Context context, AttributeSet attrs) {
	// super(context, attrs);
	// }
	// public Graph(Context context) {
	// super(context);
	// }
	// @Override
	// protected void onDraw(Canvas canvas) {
	// Log.i(Const.tag_GF, "onDraw Called");
	// super.onDraw(canvas);
	// //canvas.drawLine(0, 0, 20, 20, paint);
	// //canvas.drawLine(20, 0, 0, 20, paint);
	// }
	//
	// }
}
