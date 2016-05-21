package monitoringtool;

public interface MqttMiniCallback {
    public void connectionLost();
    public void debugArrived();
    public void connected();
}
