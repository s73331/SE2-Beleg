package monitoringtool;

/**
 * An instance of this interface must be set as callback in MqttHelper's constructor.
 * @author martin
 *
 */
public interface MqttModel {
    /**
     * Called, when MqttHelper has established a connection to the MQTT broker.
     */
    public void mqttConnected();
    /**
     * Called, when MqttHelper has lost the connection to the MQTT broker.
     */
    public void mqttConnectionLost();
    /**
     * Returns, whether the debug mode is active.
     * @return
     */
    public boolean isDebugging();
    /**
     * Called, when a debug message has arrived from the ev3steuerung.
     */
    public void debugArrived(String debug);
    /**
     * Sets the current state.
     * @param state
     */
    public void setState(String state);
}
