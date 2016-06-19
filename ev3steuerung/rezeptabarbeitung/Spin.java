package Maschinensteuerung;


public class Spin {

	private int angle;
	private int speed;
	private int device;
	private int sensor;
	private int till;

	public int getAngle() {
		return angle;
	}

	public void setAngle(int angle) {
		this.angle = angle;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}
	
	public int getDevice() {
		return device;
	}

	public void setDevice(int device) {
		this.device = device;
	}

	public int getTill() {
		return till;
	}

	public void setTill(int till) {
		this.till = till;
	}
	
	public int getSensor() {
		return sensor;
	}

	public void setSensor(int sensor) {
		this.sensor = sensor;
	}

	
	/*Drehen bis Touchsensor gedrückt(till = 1), losgelassen(till = 0) oder bis Gradzahl(till = 9)*/
	public Spin(int angle, int speed, int device, int sensor, int till){
		this.speed = speed;
		this.angle = angle;
		this.device = device;
		this.setSensor(sensor);
		this.setTill(till);
	}
	




	
}
