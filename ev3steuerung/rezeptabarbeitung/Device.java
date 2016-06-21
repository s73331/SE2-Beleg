package ev3steuerung.rezeptabarbeitung;

import lejos.utility.Delay;

public abstract class Device {
	
    
    /**
     *rotates the motor with the specified speed and angle from the recipe.
     *@return true
     */
	public boolean rotate(boolean mode,int speed, int angle){
		return true;
	}
	
    /**
     *Close the device on the specified port.
     *@return true
     */
	public boolean close(){
		return true;
	}
    
    /**
     *Registers the device with the specified port.
     *@return true
     */
	public boolean register(){
		return true;
	}
	
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

	



	
	

}
