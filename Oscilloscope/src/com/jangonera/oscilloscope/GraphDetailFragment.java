package com.jangonera.oscilloscope;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.example.oscilloscope.R;
import com.jangonera.oscilloscope.ExternalDataContainer.Probe;

public class GraphDetailFragment extends Fragment {
	
	private Probe probe;
	private final String temperature = "temperature";
	private RelativeLayout mainLayout;
	
    private GraphicalView graph;
    private XYMultipleSeriesDataset mDataset;
    private XYMultipleSeriesRenderer mRenderer;
    private XYSeries mCurrentSeries;
    private XYSeriesRenderer mCurrentRenderer;
	
    private void initChart() {
    	mDataset = new XYMultipleSeriesDataset();
    	mRenderer = new XYMultipleSeriesRenderer();
        mCurrentSeries = new XYSeries("Sample Data");
        mDataset.addSeries(mCurrentSeries);
        mCurrentRenderer = new XYSeriesRenderer();
        mCurrentRenderer.setColor(Color.WHITE);
        mCurrentRenderer.setPointStyle(PointStyle.CIRCLE);
        mCurrentRenderer.setPointStrokeWidth(3f);
        mCurrentRenderer.setLineWidth(2f);
        
        mRenderer.addSeriesRenderer(mCurrentRenderer);
        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setBackgroundColor(Color.parseColor("#307CC9"));
        mRenderer.setMarginsColor(Color.parseColor("#307CC9"));
        mRenderer.setMargins(new int[]{15,10,0,10});
        mRenderer.setYLabelsAlign(Align.RIGHT);
        mRenderer.setYLabelsPadding(2f);
    }

    private void addSampleData() {
        mCurrentSeries.add(1, 2);
        mCurrentSeries.add(2, 3);
        mCurrentSeries.add(3, 2);
        mCurrentSeries.add(4, 5);
        mCurrentSeries.add(5, 4);
    }
	
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
		mainLayout = (RelativeLayout) getActivity().findViewById(R.id.graph);
		setupChart();
        setupBackButton();
	}
		
	public void refresh() {
		graph.repaint();
	}
	
	private void setupChart() {
        initChart();
        addSampleData();
        graph = ChartFactory.getLineChartView(getActivity(), mDataset, mRenderer);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ABOVE, R.id.back_button);
        mainLayout.addView(graph, params);
	}
	
	private void setupBackButton(){
		TextView backButton = (TextView) getActivity().findViewById(R.id.back_button);
		backButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				back();
			}
		});
//		mainLayout.addView(backButton);
	}
	
	private void back() {
		((MainActivity)getActivity()).hideGraphDetails();
	}
}
