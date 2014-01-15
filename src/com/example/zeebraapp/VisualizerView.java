package com.example.zeebraapp;

import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class VisualizerView extends View {
	private static final int LINE_WIDTH = 1; // width of visualizer lines
	private static final int LINE_SCALE = 205; // scales visualizer lines
	private List<Float> amplitudes; // amplitudes for line lengths
	private int width; // width of this View
	private int height; // height of this View
	private Paint linePaint; // specifies line drawing characteristics

	// constructor

	public VisualizerView(Context context, AttributeSet attrs) {
		super(context, attrs); // call superclass constructor
		linePaint = new Paint(); // create Paint for lines
		linePaint.setColor(Color.RED); // set color to green
		linePaint.setStrokeWidth(LINE_WIDTH); // set stroke width
	} // end VisualizerView constructor

	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		width = w; // new width of this View
		height = h; // new height of this View
//		height = 1250;
		amplitudes = new ArrayList<Float>(width / LINE_WIDTH);
	}

	public void clear() {
		amplitudes.clear();
	} // end method clear
		// add the given amplitude to the amplitudes ArrayList

	public void addAmplitude(float amplitude) {
		amplitudes.add(amplitude); // add newest to the amplitudes ArrayList
		// if the power lines completely fill the VisualizerView
		if (amplitudes.size() * LINE_WIDTH >= width) {
			amplitudes.remove(0); // remove oldest power value
		} // end if
	} // end method addAmplitude

	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
	    int desiredHeight = 150;

	    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
	    int heightSize = MeasureSpec.getSize(heightMeasureSpec);

	    int height;
		
		   //Measure Height
	    if (heightMode == MeasureSpec.EXACTLY) {
	        //Must be this size
	        height = heightSize;
	    } else if (heightMode == MeasureSpec.AT_MOST) {
	        //Can't be bigger than...
	        height = Math.min(desiredHeight, heightSize);
	    } else {
	        //Be whatever you want
	        height = desiredHeight;
	    }

	    //MUST CALL THIS
	    setMeasuredDimension(widthMeasureSpec, height);
	}
	
	@SuppressLint("DrawAllocation")
	@Override
	public void onDraw(Canvas canvas) {
		
		  Paint paint = new Paint();
		  paint.setColor( Color.BLACK );
		  paint.setStrokeWidth( 1.5f );
		  paint.setStyle( Style.STROKE );
//
//		  canvas.drawRect( 0, 0, getWidth(), getHeight(), paint );
		  
		  canvas.drawLine(0, getHeight(), getWidth(), getHeight(), paint);
		
		
		
		int middle = height / 2; // get the middle of the View
		float curX = 0; // start curX at zero
		// for each item in the amplitudes ArrayList
		for (float power : amplitudes) {
			float scaledHeight = power / LINE_SCALE; // scale the power
			curX += LINE_WIDTH; // increase X by LINE_WIDTH
			// draw a line representing this item in the amplitudes ArrayList
			canvas.drawLine(curX, middle + scaledHeight / 2, curX, middle
					- scaledHeight / 2, linePaint);
		} // end for
	} // end method onDraw
} // end class VisualizerView

