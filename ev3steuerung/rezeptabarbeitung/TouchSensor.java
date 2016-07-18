package ev3steuerung.rezeptabarbeitung;

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3TouchSensor;

public class TouchSensor extends SensorDevice {
    
    private String port;
    private EV3TouchSensor sensor;
    private SimpleTouch touch;
    
     /**
     *
     *@return port
     */
    public String getPort() {
        return port;
    }

     /**
     *@param port on which the TouchSensor is connected
     */
    public void setPort(String port) {
        this.port = port;
    }
    
      /**
     *Constructor
     *@param port on which the TouchSensor is connected
     */
    public TouchSensor(String port){
        this.port = port;
    }
    
     /**
     *Register the TouchSensor with the specified port in the recipe.
     *Create the TouchSensor.
     *@return true
     */
    @Override
    public boolean register(){
        boolean isOk = true;
        try {
            Brick brick = BrickFinder.getDefault();
            Port p = brick.getPort(port);
            sensor = new EV3TouchSensor(p);
            touch = new SimpleTouch(sensor);
        } catch (Exception e) {
            e.printStackTrace();
            isOk = false;
        } finally {
            return isOk;
        }
    }
    
    /**
    * Checks if the TouchSensor is pressed
    * @return True - When the TouchSensor is pressed
    */
    @Override
    public boolean isPressed(){
        return touch.isPressed();
    }
    
    /**
    * Checks if the TouchSensor is not pressed
    * @return True - When the TouchSensor is pressed
    */
    @Override
    public boolean isNotPressed(){
        return !touch.isPressed();
    }
    
    /*
    * Waits until the TouchSensor is pressed.
    * Checks every 50 ms if the TouchSensor is pressed.
    * @return false
    @Override
    public boolean waitForPress(){
        int time = 0;
        while (!touch.isPressed() && time < 10000) {
              Delay.msDelay(50);
              time += 50;
            }
        return false;
    }
    
    /**
    * Waits until the TouchSensor is released.
    * Checks every 50 ms if the TouchSensor is released.
    * @return false
    *
    @Override
    public boolean waitForRelease(){
        
        while (touch.isPressed()) {
              Delay.msDelay(50);
            }
        return false;
    }
    
    */
    /**
     *Close the TouchSensor with the specified port.
     *@return true
     */
    @Override
    public boolean close(){
        sensor.close();
        return true;
    }
    /**
     * Getter for the SimpleTouch object
     * 
     * @return touch - Simple Touch object
     * @see Recipe.work()
     */
    @Override
    public SimpleTouch getSensor() {
        return touch;
    }

}
