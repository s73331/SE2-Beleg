package Maschinensteuerung;

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.utility.Delay;

public class TouchSensor extends Device {
	
	private String port;
	private EV3TouchSensor sensor;
	private SimpleTouch touch;
	
	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}
	
	public TouchSensor(String port){
		this.port = port;
	}
	
	@Override
	public boolean register(){
		
		Brick brick = BrickFinder.getDefault();
		Port p = brick.getPort(port);
		sensor = new EV3TouchSensor(p);
		touch = new SimpleTouch(sensor);

		return true;
	}
	
	@Override
	public boolean isPressed(){

		return touch.isPressed();
	}
	
	@Override
	public boolean isNotPressed(){

		return !touch.isPressed();
	}
	
	@Override
	public boolean waitForPress(){
		
		while (!touch.isPressed()) {
		      Delay.msDelay(50);
		    }
		return false;
	}
	
	@Override
	public boolean waitForRelease(){
		
		while (touch.isPressed()) {
		      Delay.msDelay(50);
		    }
		return false;
	}
	
	@Override
	public boolean close(){
		sensor.close();
		return true;
	}



}
