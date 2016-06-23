package ev3steuerung.rezeptabarbeitung;

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.Color;

public class ColorSensor extends SensorDevice{
    
    private String port;
    private EV3ColorSensor sensor;
    
    /**
     *
     * @return String respresentation of the Port
     */
    public String getPort() {
        return port;
    }
    
    /**
     *@param port on which the ColorSensor is connected
     */
    public void setPort(String port) {
        if(port == null) throw new IllegalArgumentException("port must not be null");
        this.port = port;
    }
    
    /**
     * Constructor
     * @param port on which the ColorSensor is connected
     * @throws NullPointerException - When port is null
     * @throws IllegalArgumentException - When port is empty or longer than 2 Characters
     */
    public ColorSensor(String port) {
        if(port == null)
            throw new NullPointerException("port must not be null");
        if(port.isEmpty() || port.length() > 2)
            throw new IllegalArgumentException("port must not be null");
        this.port = port;
    }
    
    /**
     *Register the ColorSensor with the specified port in the recipe.
     *Create the ColorSensor.
     *@return true
     */
    @Override
    public boolean register(){
        boolean isOk = true;
        try {
            Brick brick = BrickFinder.getDefault();
            Port p = brick.getPort(port);
            sensor = new EV3ColorSensor(p);
        } catch (Exception e) {
            e.printStackTrace();
            isOk = false;
        } finally {
            return isOk;
        }
    }
    
    /**
     *Detect the color on the ColorSensor and display the detected color on the LCD of the EV3.
     *@return true
     */
    @Override
    public boolean detectColor(){
        
        SensorMode color = sensor.getColorIDMode();
        float[] sample = new float[color.sampleSize()];
        color.fetchSample(sample, 0);
        int colorId = (int)sample[0];
        String colorName = "";
        switch(colorId){
            case Color.NONE: colorName = "NONE"; break;
            case Color.BLACK: colorName = "BLACK"; break;
            case Color.BLUE: colorName = "BLUE"; break;
            case Color.GREEN: colorName = "GREEN"; break;
            case Color.YELLOW: colorName = "YELLOW"; break;
            case Color.RED: colorName = "RED"; break;
            case Color.WHITE: colorName = "WHITE"; break;
            case Color.BROWN: colorName = "BROWN"; break;
        }
        LCD.drawString(colorId + " - " + colorName, 1, 3);

        
        return true;
        
    }
    /**
     *Close the ColorSensor with the specified port.
     *@return true
     */
    public boolean close(){
        sensor.close();
        return true;
    }
    
    
    

}
