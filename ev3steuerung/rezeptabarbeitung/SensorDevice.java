package ev3steuerung.rezeptabarbeitung;


/**
 * Beschreiben Sie hier die Klasse SensorDevice.
 * 
 * @author (Ihr Name) 
 * @version (eine Versionsnummer oder ein Datum)
 */
public abstract class SensorDevice implements Device
{
    
    /**
     *Checks if the TouchSensor is pressed
     *@return true
     */
    public boolean isPressed(){
        return true;
    }
    
    /**
     *Waits until the TouchSensor is pressed.
     *@return true
     */
    public boolean waitForPress(){
        return true;
    }
    
    /**
     *Waits until the TouchSensor is released.
     *@return true
     */
    public boolean waitForRelease(){
        return true;
    }
    
    /**
     *Detect the color on the ColorSensor and display the detected color on the LCD of the EV3.
     *@return false
     */
    public boolean detectColor() {
        return false;
    }

    /**
     *Checks if the TouchSensor is not pressed
      *@return false
     */
    public boolean isNotPressed() {
        return false;
    }

       
    public SimpleTouch getSensor() {
       return null;
       }
}
