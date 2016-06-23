package ev3steuerung.rezeptabarbeitung;

import lejos.utility.Delay;
import lejos.hardware.motor.BaseRegulatedMotor;

/**
 * Beschreiben Sie hier die Klasse MotorDevice.
 * 
 * @author (Ihr Name) 
 * @version (eine Versionsnummer oder ein Datum)
 */
public abstract class MotorDevice implements Device
{
    /**
     *rotates the motor with the specified speed and angle from the recipe.
     *@return true
     */
    public boolean rotate(boolean mode,int speed, int angle){
        return false;
    }
    
    /**
     *rotates the motor with the specified speed from the recipe
     *no defined angle
     *@return true
     */
    public boolean forward(int speed) {
        return false;
    }

    /**
     *stops the device
     *@return false
     */
    public boolean stop() {
        return false;
    }
    
    /**
     * Returns a BaseRegulatedMotor Object
     * 
     * @return BaseRegulatedMotor
     * @see lejos.hardware.motor.BaseRegulatedMotor
     * @see Recipe
     */
    public BaseRegulatedMotor getEV3Motor(){
        return null;
    }
}
