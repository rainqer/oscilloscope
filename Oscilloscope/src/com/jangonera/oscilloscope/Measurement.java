package com.jangonera.oscilloscope;

import java.util.Date;

public class Measurement {
	private Date date;
	private double value;
	
	public Measurement(Date date, double value){
		this.date = date;
		this.value = value;
	}
	
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
}
