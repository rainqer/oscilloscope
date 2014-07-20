package com.jangonera.oscilloscope;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataFileExporter {

	public final static String datePattern = "yyyy:MM:dd-HH:mm:ss";
	public final static String format = "%s time:%s temperature:%s humidity:%s\n";
	public final static String dataFile = "/sdcard/mysdfile.txt";
	
	public void storeData(String address, double temperature, double humidity) {
		try {
            File myFile = new File(dataFile);
           
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile, true);
            OutputStreamWriter myOutWriter = 
                                    new OutputStreamWriter(fOut);
            myOutWriter.append(createLine(address, temperature, humidity));
            
            myOutWriter.close();
            fOut.close();
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	private String createLine(String address, double temperature, double humidity) {
		String temperatureString = Double.toString(temperature);
		String humidityString = Double.toString(humidity);
		SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
		return String.format(format, address, dateFormat.format(new Date()), temperatureString, humidityString);
	}
	
}
