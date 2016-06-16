package ev3steuerung;



public interface MqttBrick {
    /**
     * @return "IDLE", "PROC" or "MAINT"
     */
    public String getState();
    /**
     * user pressed fix button in gui (or this message arrived because of something else)
     */
    public void manualFix();
    /**
     * user pressed shutdown button in gui (or this message arrived because of something else)
     */
    public void emergencyShutdown();
    /**
     * this always came from topic vwp/toolid as mqtthelper handles everything else
     * the messages are directly forwarded to this function, no checks are done
     * @param message
     */
    public void messageArrived(String message);
}