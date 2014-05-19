package com.jangonera.oscilloscope;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.jangonera.oscilloscope.ExternalDataContainer.Probe;

//Graph represents the area on which a ready-probe information is drawn
public class Graph extends View {
	private Paint paint;
	private Probe readyProbe;
	private int voltY = 3; //1 = 1 volt, above and below zero
	private int scaleY = 50; //how many pixels per volt
	private final int GRID_WIDTH = 3;
	private final int TEXT_SIZE = 8;
	private final int MARGIN_LEFT = 15;
	private final int MARGIN_TOP = 5;
	
	public Graph(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initPaint();
	}

	public Graph(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPaint();
	}

	public Graph(Context context) {
		super(context);
		initPaint();
	}
	
	private void initPaint(){
		paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setTextSize(TEXT_SIZE);
	}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (canvas != null) {
			//canvas.drawLine(0, 0, 20, 20, paint);
			//canvas.drawLine(20, 0, 0, 20, paint);
			drawGrid(canvas);
			
			if(readyProbe == null) return;
			//calculate the width of a single time step
			int time = 0;
			float timeBase = canvas.getWidth()/readyProbe.getListLength(); 
			int previous = 0;
			for(int value : readyProbe.getValues()){
				if(time != 0){
					//canvas.drawLine((time-1)*timeBase, previous, (time)*timeBase, value, paint);
					drawLine((time-1)*timeBase, previous, (time)*timeBase, value, canvas);
				}
				previous = value;
				time++;
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    int desiredWidth = 100;

	    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
	    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
	    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
	    int heightSize = MeasureSpec.getSize(heightMeasureSpec);

	    int width;
	    int height;

	    //Measure Width
	    if (widthMode == MeasureSpec.EXACTLY) {
	        width = widthSize;
	    } else {
	        width = desiredWidth;
	    }

	    //Measure Height
	    if (heightMode == MeasureSpec.EXACTLY) {
	        height = heightSize;
	    } else {
	        height = scaleY * (voltY *2 + 1) +MARGIN_TOP;
	    }
	    setMeasuredDimension(width, height);
	}
	
	private void drawGrid(Canvas canvas){
		int textDecrease = (TEXT_SIZE / 2) - 1  + MARGIN_TOP;
		
		
		drawGraphLine(0, voltY * scaleY, canvas.getWidth(), voltY * scaleY, canvas);
		//x axis
		canvas.drawText("0", 0, voltY*scaleY + textDecrease, paint);
		
		//grid
		for(int value = 1; value <= voltY; ++value){
			
			drawGraphLine(0, (voltY - value)*scaleY, GRID_WIDTH, (voltY - value)*scaleY, canvas);
			canvas.drawText(Integer.toString(value), 0, (voltY - value)*scaleY + textDecrease, paint);
		
			drawGraphLine(0, (voltY + value)*scaleY, GRID_WIDTH, (voltY + value)*scaleY, canvas);
			canvas.drawText(Integer.toString(-value), 0, (voltY + value)*scaleY + textDecrease, paint);
			
		}
		//y axis
		drawGraphLine(GRID_WIDTH, 0, GRID_WIDTH, (voltY+ voltY)*scaleY, canvas);
		
		
		/*
		canvas.drawLine(MARGIN_LEFT, voltY * scaleY + MARGIN_TOP, canvas.getWidth(), voltY * scaleY + MARGIN_TOP, paint);
		canvas.drawText("0", 0, voltY*scaleY + textDecrease, paint);
		
		for(int value = 1; value <= voltY; ++value){
			
			canvas.drawLine(MARGIN_LEFT, (voltY - value)*scaleY + MARGIN_TOP, MARGIN_LEFT + GRID_WIDTH, (voltY - value)*scaleY + MARGIN_TOP, paint);
			canvas.drawText(Integer.toString(value), 0, (voltY - value)*scaleY + textDecrease, paint);
		
			canvas.drawLine(MARGIN_LEFT, (voltY + value)*scaleY + MARGIN_TOP, MARGIN_LEFT + GRID_WIDTH, (voltY + value)*scaleY + MARGIN_TOP, paint);
			canvas.drawText(Integer.toString(-value), 0, (voltY + value)*scaleY + textDecrease, paint);
			
		}
		canvas.drawLine(MARGIN_LEFT+GRID_WIDTH, MARGIN_TOP, MARGIN_LEFT+GRID_WIDTH, (voltY+ voltY)*scaleY + MARGIN_TOP, paint);
		 */
	}
	
	private void drawGraphLine(int x1, int y1, int x2, int y2, Canvas canvas){
		canvas.drawLine(MARGIN_LEFT + x1, MARGIN_TOP + y1, MARGIN_LEFT + x2, MARGIN_TOP + y2, paint);
	}
	private void drawLine(float x1, float y1, float x2, float y2, Canvas canvas){
		float zeroLevel = scaleY * voltY;
		canvas.drawLine(MARGIN_LEFT + GRID_WIDTH + x1, MARGIN_TOP + zeroLevel - y1, MARGIN_LEFT + GRID_WIDTH + x2, MARGIN_TOP + zeroLevel - y2, paint);
	}
	
	
	public void registerProbe(Probe readyProbe) {
		this.readyProbe = readyProbe;
//		if(this.readyProbe!=null)
//			this.readyProbe.registerGraph(this);
	}
}
