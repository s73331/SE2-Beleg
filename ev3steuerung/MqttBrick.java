package ev3steuerung;

/**
 * Interface to make Mqtt-Functionality available
 * 
 * @author Christoph Schmidt
 * @version 0.9
 */

public interface MqttBrick
{
    /**
     * Method to start MQTT-Handling by creating a
     * new MqttHelper Class and setting mqttHelper
     * 
     * @see MqttHelper
     * @throws InterruptedException When there was a problem creating the mqtt-handler */
    void startMqtt() throws InterruptedException;
    /**
     *  Method to call from MqttHelper when "manual fix" message arrives.
     *  This sets a fix flag, if the current State is instanceof Maint
     *  
     *  @see Maint
     *  @see MqttHelper */
    void manualFix();
    /**
     *  Method to call from MqttHelper when "emergency shutdown" message arrives.
     *  This sets the state forcibly to ShuttingDown
     *  
     *  @see ShuttingDown
     *  @see MqttHelper */
    void emergencyShutdown();
    /**
     *  Method to call when a message is recieved on the Topic vwp/DEVICE_ID.
     *  No Checks have been made to the input.
     *  Handles produce, confirm, sleep
     *  
     *  @param  message Message that arrived over Mqtt from MES 
     *  @see MqttHelper
     *  @see Mqtt-Threads 
     *  @see Recipe
     */
    void messageArrived(String message);
     /**
     * Stops the Mqtt-Handler, closes all connections and deletes all folders
     * 
     * @see Control
     * @see MqttHelper.close() */
    void stopMqtt();
    /**
     * 
     */
    String getStateName();
}
