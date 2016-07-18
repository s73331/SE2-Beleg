package ev3steuerung.rezeptabarbeitung;

import ev3steuerung.rezeptabarbeitung.Flag.DevicePort;
import ev3steuerung.rezeptabarbeitung.Flag.SpinMode;


public class Spin {

	private int angle;
	private int speed;
	private int device;
	private int sensor;
	private SpinMode mode;

    public SpinMode getMode() {
		return mode;
	}

	public void setMode(SpinMode mode) {
		this.mode = mode;
	}

	/**
     *@return angle
     */
	public int getAngle() {
		return angle;
	}

    /**
     *@param angle for how far the motors spins
     */
	public void setAngle(int angle) {
		this.angle = angle;
	}

    /**
     *@return speed
     */
	public int getSpeed() {
		return speed;
	}

    /**
     *@param speed for the motor in angle per second
     */
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	
    /**
     *@return device
     */
	public int getDevice() {
		return device;
	}

    /**
     *@param device. Which motor should spin
     */
	public void setDevice(int device) {
		this.device = device;
	}
	
    /**
     *@return sensor
     */
	public int getSensor() {
		return sensor;
	}

    /**
     * @param sensor on which the motor should react for press/release
     */
	public void setSensor(int sensor) {
		this.sensor = sensor;
	}

	/**
     * Constructor
     * @param angle - What Angle it shall turn to
     * @param speed - What Speed it should use
     * @param device - What MotorPort it shall use
     * @param sensor - What sensor on which the motor should react for press/release
     * @param till - TODO: No Idea
     */
	public Spin(int speed, DevicePort device, DevicePort sensor, SpinMode mode){
		this.speed = speed;
		this.device = device.ordinal();
		this.sensor = sensor.ordinal();
		this.mode = mode;
	}

	public Spin(int angle, int speed, DevicePort device, SpinMode mode){
		this.speed = speed;
		this.angle = angle;
		this.device = device.ordinal();
		this.mode = mode;
	}

	
}
