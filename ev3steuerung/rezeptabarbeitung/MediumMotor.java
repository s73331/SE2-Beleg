package ev3steuerung.rezeptabarbeitung;

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;

public class MediumMotor extends Device {
	
	private String port;
	private EV3MediumRegulatedMotor motor;
	
	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}
	
	public MediumMotor(String port){
		this.port = port;
	}
	
	@Override
	public boolean register (){
		
		Brick brick = BrickFinder.getDefault();
		Port p = brick.getPort(port);
		motor = new EV3MediumRegulatedMotor(p);
		return true;
	}
	
	@Override
	public boolean rotate(boolean mode,int speed, int angle ){
		
		if(speed <= (int) motor.getMaxSpeed()){
			motor.setSpeed(speed);
		}
		else{
			//TODO Fehler ausgeben und return false
			// Warnung oder Abbruch
			motor.setSpeed(motor.getMaxSpeed());
		}
		
		motor.resetTachoCount();
		motor.rotate(angle,mode);
		
		return true;
		
	}
	
	@Override
	public boolean forward(int speed){
		
		if(speed <= (int) motor.getMaxSpeed()){
			motor.setSpeed(speed);
		}
		else{
			//TODO Fehler ausgeben und return false
			// Warnung oder Abbruch
			motor.setSpeed(motor.getMaxSpeed());
		}
		
		motor.resetTachoCount();
		motor.forward();
		
		return true;
	}
	
	@Override
	public boolean stop(){
		motor.stop();
		return true;
	}
	
	@Override
	public boolean close(){
		motor.close();
		return true;
	}



}
