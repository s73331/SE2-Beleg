package ev3steuerung.rezeptabarbeitung;

import lejos.utility.Delay;

public class Wait {
	
	private long ms;
	private int mode;	// 0 = auf bestimmte Zeit warten, 1 = auf TouchSensor warten (drücken)
						// 2 = auf TouchSensor warten (los lassen)
	private int sensor;

	public long getMs() {
		return ms;
	}
	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}
	public void setMs(long ms) {
		this.ms = ms;
	}
	public int getSensor() {
		return sensor;
	}
	public void setSensor(int sensor) {
		this.sensor = sensor;
	}
	
	public Wait(long ms, int mode){
		this.ms = ms;
		this.mode = mode;
	}
	public Wait(int sensor, int mode){
		this.sensor = sensor;
		this.mode = mode;
	}
	
	
	
	public boolean waitTime() {
		Delay.msDelay(ms);
		return true;
	}
	

	
}
