package com.jangonera.oscilloscope;

import java.util.LinkedList;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
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
	
	public final static String timeFormat = "H:mm:ss";
	
	private Probe probe;
	private final String temperature = "temperature";
	private RelativeLayout mainLayout;
	
    private GraphicalView graph;
    private XYMultipleSeriesDataset mDataset;
    private XYMultipleSeriesRenderer mRenderer;
    private TimeSeries mCurrentSeries;
    private XYSeriesRenderer mCurrentRenderer;
    private TextView humtembutton;
    
    public enum DataType {
    	TEMPERATURE, HUMIDITY
    }
    private DataType dataType;
    public void swap(DataType dataType) {
    	this.dataType = dataType;
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
        setupButtons();
	}
    
    private void initChart() {
    	mDataset = new XYMultipleSeriesDataset();
    	mRenderer = new XYMultipleSeriesRenderer();
        mCurrentRenderer = new XYSeriesRenderer();
        mCurrentRenderer.setColor(Color.WHITE);
        mCurrentRenderer.setPointStyle(PointStyle.CIRCLE);
//        mCurrentRenderer.setPointStrokeWidth(0.75f);
        mCurrentRenderer.setFillPoints(true);
        mCurrentRenderer.setLineWidth(1f);
        mRenderer.addSeriesRenderer(mCurrentRenderer);
        mRenderer.setApplyBackgroundColor(true);
        if(dataType != dataType.HUMIDITY) {
        	mRenderer.setBackgroundColor(getResources().getColor(R.color.blue1));
        	mRenderer.setMarginsColor(getResources().getColor(R.color.blue1));
        }
        else {
        	mRenderer.setBackgroundColor(getResources().getColor(R.color.gray_light));
        	mRenderer.setMarginsColor(getResources().getColor(R.color.gray_light));
        }
        mRenderer.setMargins(new int[]{15,30,0,10});
        mRenderer.setYAxisColor(Color.WHITE);
        mRenderer.setXAxisColor(Color.WHITE);
        mRenderer.setXLabelsColor(Color.WHITE);
        mRenderer.setYLabelsColor(0, Color.WHITE);
        mRenderer.setYLabelsAlign(Align.RIGHT);
        mRenderer.setYLabelsPadding(2f);
        mRenderer.setPointSize(1.75f);
        mRenderer.setShowGrid(true);
        mRenderer.setGridColor(getResources().getColor(R.color.blue_grid));
        mRenderer.setFitLegend(true);
    }

    private void addData() {
    	if(probe == null) return;
    	mDataset.removeSeries(mCurrentSeries);
        mCurrentSeries = new TimeSeries(temperature);
        LinkedList<Measurement> measurements;
        if(dataType != dataType.HUMIDITY) {
        	measurements = probe.getValuesTemperature();
        }
        else {
        	measurements = probe.getValuesHumidity();
        }
        for(Measurement measurement : measurements) {
    		mCurrentSeries.add(measurement.getDate(), measurement.getValue());
    	}
        mDataset.addSeries(mCurrentSeries);
    }
	
	public void registerProbe(Probe readyProbe) {
		if(probe != null) probe.registerGraph(null);
		probe = readyProbe;
		if(probe != null) probe.registerGraph(this);
	}
	
	public synchronized void refresh() {
		addData();
		graph.repaint();
	}
	
	public void refit() {
		mainLayout.removeView(graph);
		setupChart();
	}
	
	private synchronized void setupChart() {
        initChart();
        addData();
        graph = ChartFactory.getTimeChartView(getActivity(), mDataset, mRenderer, timeFormat);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ABOVE, R.id.buttons);
        mainLayout.addView(graph, params);
	}
	
	private void setupButtons(){
		TextView backButton = (TextView) getActivity().findViewById(R.id.back_button);
		backButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				back();
			}
		});
		
		
		humtembutton = (TextView) getActivity().findViewById(R.id.humtem_button);
		humtembutton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(dataType == DataType.HUMIDITY){
					swap(DataType.TEMPERATURE);
					humtembutton.setText(getResources().getText(R.string.hum_button));
			    	refit();
				}
				else {
					swap(DataType.HUMIDITY);
					humtembutton.setText(getResources().getText(R.string.tem_button));
					refit();
				}
			}
		});
		
		TextView refitButton = (TextView) getActivity().findViewById(R.id.refit_button);
		refitButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				refit();
			}
		});
	}
	
	private void back() {
		((MainActivity)getActivity()).hideGraphDetails();
	}
}
