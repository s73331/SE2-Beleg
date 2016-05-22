package monitoringtool;

public interface MqttModel {
    public void mqttConnected();
    public void mqttConnectionLost();
    public boolean isDebugging();
    public void debugArrived();
    public void addDebug(String string);
    public void reportedOnline();
    public void reportedOffline();
}
