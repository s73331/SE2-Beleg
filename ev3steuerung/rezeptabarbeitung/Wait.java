package ev3steuerung.rezeptabarbeitung;

import lejos.utility.Delay;

public class Wait {
	
	private long ms;
	private int mode;
	private int sensor;

    /**
     *@return ms
     */
	public long getMs() {
		return ms;
	}
    
    /**
     *@param ms for the waiting time in ms(milliseconds)
     */
    public void setMs(long ms) {
        this.ms = ms;
    }
    
    /**
     *@return mode
     */
	public int getMode() {
		return mode;
	}

    /**
     *Set the waiting type
     *0 = wait for specified time in ms
     *1 = wait for pressing the TouchSensor
     *2 = wait for releasing the TouchSensor
     *@param mode
     */
	public void setMode(int mode) {
		this.mode = mode;
	}
    
    /**
     *@return sensor
     */
	public int getSensor() {
		return sensor;
	}
    
    /**
     *@param sensor on which should be waited
     */
	public void setSensor(int sensor) {
		this.sensor = sensor;
	}
	
    /**
     *Constructor
     *used for time waiting
     *@param ms
     *@param mode
     */
	public Wait(long ms, int mode){
		this.ms = ms;
		this.mode = mode;
	}
    
    /**
     *Constructor
     *used for sensor waiting
     *@param sensor
     *@param mode
     */
	public Wait(int sensor, int mode){
		this.sensor = sensor;
		this.mode = mode;
	}
	
	
	/**
     *Starts the waiting process defined in tims(ms)
     *@return true
     */
	public boolean waitTime() {
		Delay.msDelay(ms);
		return true;
	}
	

	
}
