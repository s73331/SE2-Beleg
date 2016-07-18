package ev3steuerung.rezeptabarbeitung;

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.BaseRegulatedMotor;
import lejos.hardware.port.Port;

public class LargeMotor extends MotorDevice {
    
    private String port;
    private EV3LargeRegulatedMotor motor;
    
    /**
     * Getter for the Port of the Motor
     * 
     * @return port of the LargeMotor
     */
    public String getPort() {
        return this.port;
    }

    /**
     * Sets the Port where the MediumMotor is connected
     * 
     * @param Port on which the MediumMotor is connected
     */
    public void setPort(String port) {
        this.port = port;
    }
    
    /**
     * Constructor
     * @param port on which the LargeMotor is connected
     */
    public LargeMotor(String port){
        this.port = port;
    }
    
    /**
     * Register the LargeMotor with the specified Port in the Recipe.
     * Create the LargeMotor.
     * 
     * @return True - When registering worked
     */
    @Override
    public boolean register(){
        boolean isOk = true;
        try {
            Brick brick = BrickFinder.getDefault();
            Port p = brick.getPort(port);
            motor = new EV3LargeRegulatedMotor(p);
        } catch (Exception e) {
            e.printStackTrace();
            isOk = false;
        } finally {
            return isOk;
        }
    }
    
    /**
     * rotates the motor with the specified speed and angle from the recipe.
     * if the specified speed is smaller than the maxSpeed (getMaxSpeed) of the LargeMotor, then use it
     * else use the maxSpeed from the LargeMotor
     * @return True
     */
    @Override
    public boolean rotate(boolean mode,int speed, int angle ){
        
        if(speed <= (int) motor.getMaxSpeed()){
        	System.out.println("Motorspeed Large ok");
            motor.setSpeed(speed);
        }
        else{
            //TODO Fehler ausgeben und return false
            // Warnung oder Abbruch
        	System.out.println("Motorspeed Large ok");
        	 
            motor.setSpeed(motor.getMaxSpeed());
        }
        
        motor.rotate(angle,mode);
        
        return true;
        
    }
    
    /**
     * rotates the motor with the specified speed from the recipe
     * if the specified speed is smaller than the maxSpeed (getMaxSpeed) of the LargeMotor, then use it
     * else use the maxSpeed from the LargeMotor
     * @return True
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
        
        motor.forward();
        
        return true;
    }
    
    /**
     * stops the motor
     * @return True
     */
    @Override
    public boolean stop(){
        motor.stop();
        return true;
    }
    
    /**
     * Close the MediumMotor with the specified port.
     * @return True - If the Motor and his Port could be closed successfuzlly
     */
    @Override
    public boolean close(){
        motor.close();
        return true;
    }
    
    /**
     * Getter for the Motor Object
     * 
     * @return motor - The motor object
     * @see Recipe
     */
    public BaseRegulatedMotor getEV3Motor() {
        return this.motor;
    }

}
