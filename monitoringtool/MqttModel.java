package monitoringtool;

public interface MqttModel {
    void mqttConnected();
    void mqttConnectionLost();
    boolean isDebugging();
    void debugArrived();
    void addDebug(String string);
}
