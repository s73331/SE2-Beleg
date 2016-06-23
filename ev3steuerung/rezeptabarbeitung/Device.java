package ev3steuerung.rezeptabarbeitung;

public interface Device {
    
    /**
     *Close the device on the specified port.
     *@return true
     */
    public boolean close();
    
    /**
     *Registers the device with the specified port.
     *@return true
     */
    public boolean register();
}
