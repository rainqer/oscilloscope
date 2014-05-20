package com.jangonera.oscilloscope;

import java.util.Date;

public class Measurement {
	private Date date;
	private int value;
	
	public Measurement(Date date, int value){
		this.date = date;
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
}
