package ev3steuerung;

public interface MqttBrick {
    public String getState();
    public void manualFix();
    public void emergencyShutdown();
}
