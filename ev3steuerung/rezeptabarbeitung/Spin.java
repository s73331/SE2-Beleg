package ev3steuerung.rezeptabarbeitung;


public class Spin {

	private int angle;
	private int speed;
	private int device;
	private int sensor;
	private int till;

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
     *The mode for the motor and sensor coorporation.
     *0 = spin till TouchSensor is released
     *1 = Spin till TouchSensor is pressed
     *9 = Spin the specified angle
     *@return till
     */
	public int getTill() {
		return till;
	}

    /**
     *The mode for the motor and sensor coorporation.
     *0 = spin till TouchSensor is released
     *1 = Spin till TouchSensor is pressed
     *9 = Spin the specified angle
     *@param till
     */
	public void setTill(int till) {
		this.till = till;
	}
	
    /**
     *@return sensor
     */
	public int getSensor() {
		return sensor;
	}

    /**
     *@param sensor on which the motor should react for press/release
     */
	public void setSensor(int sensor) {
		this.sensor = sensor;
	}

	/**
     *Constructor
     *@param speed
     *@param angle
     *@param device
     *@param sensor
     *@param till
     */
	public Spin(int angle, int speed, int device, int sensor, int till){
		this.speed = speed;
		this.angle = angle;
		this.device = device;
		this.setSensor(sensor);
		this.setTill(till);
	}

	
}
