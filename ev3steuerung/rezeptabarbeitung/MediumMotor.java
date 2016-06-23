package ev3steuerung.rezeptabarbeitung;

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.motor.BaseRegulatedMotor;
import lejos.hardware.port.Port;

public class MediumMotor extends MotorDevice {
    
    private String port;
    private EV3MediumRegulatedMotor motor;
    
    /**
     * Getter for the Port of the Motor
     * 
     * @return port of the MediumMotor
     */
    public String getPort() {
        return port;
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
     * @param port on which the MediumMotor is connected
     */
    public MediumMotor(String port){
        this.port = port;
    }
    
    /**
     * Register the MediumMotor with the specified port in the recipe.
     * Create the MediumMotor.
     * @return True - When registering worked
     */
    @Override
    public boolean register (){
        boolean isOk = true;
        try {
             Brick brick = BrickFinder.getDefault();
             Port p = brick.getPort(port);
             motor = new EV3MediumRegulatedMotor(p);
        } catch (Exception e) {
            e.printStackTrace();
            isOk = false;
        } finally {
            return isOk;
        }
    }
    
    /**
     * rotates the motor with the specified speed and angle from the recipe.
     * if the specified speed is smaller than the maxSpeed (getMaxSpeed) of the MediumMotor, then use it
     * else use the maxSpeed from the MediumMotor
     * @return True
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
        
        int currentAngle = motor.getTachoCount(); // should be -360
        
        return true;
        
    }
    
    /**
     * rotates the motor with the specified speed from the recipe
     * if the specified speed is smaller than the maxSpeed (getMaxSpeed) of the MediumMotor, then use it
     * else use the maxSpeed from the MediumMotor
     *
     * @param speed Value how fast the Motor should turn
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
        
        motor.resetTachoCount();
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
