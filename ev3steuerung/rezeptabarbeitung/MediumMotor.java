package ev3steuerung.rezeptabarbeitung;

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.motor.BaseRegulatedMotor;
import lejos.hardware.port.Port;

public class MediumMotor extends Device {
	
	private String port;
	private EV3MediumRegulatedMotor motor;
	
    /**
     *
     *@return port
     */
	public String getPort() {
		return port;
	}
    
    /**
     *@param port on which the MediumMotor is connected
     */
	public void setPort(String port) {
		this.port = port;
	}
	
    /**
     *Constructor
     *@param port on which the MediumMotor is connected
     */
	public MediumMotor(String port){
		this.port = port;
	}
	
    /**
     *Register the MediumMotor with the specified port in the recipe.
     *Create the MediumMotor.
     *@return true
     */
	@Override
	public boolean register (){
		
		Brick brick = BrickFinder.getDefault();
		Port p = brick.getPort(port);
		motor = new EV3MediumRegulatedMotor(p);
		return true;
	}
	
    /**
     *rotates the motor with the specified speed and angle from the recipe.
     *if the specified speed is smaller than the maxSpeed (getMaxSpeed) of the MediumMotor, then use it
     *else use the maxSpeed from the MediumMotor
     *@return true
     */
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
	
    /**
     *rotates the motor with the specified speed from the recipe
     *if the specified speed is smaller than the maxSpeed (getMaxSpeed) of the MediumMotor, then use it
     *else use the maxSpeed from the MediumMotor
     *@return true
     */
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
	
    /**
     *stops the motor
     *@return true
     */
	@Override
	public boolean stop(){
		motor.stop();
		return true;
	}
	
    /**
     *Close the MediumMotor with the specified port.
     *@return true
     */
    @Override
    public boolean close(){
        motor.close();
        return true;
    }
    
    public BaseRegulatedMotor getEV3Motor() {
        return this.motor;
    }

}
