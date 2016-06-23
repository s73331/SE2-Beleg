package ev3steuerung;

/**
 * Interface for Property-Loading
 * 
 * @author Christoph Schmidt
 * @version 0.9
 */

public interface PropertyLoader
{
    /**
     * Getter Function
     * 
     * @see EV3_Brick.initializeProperties()
     * @return The DEVICE_ID of the Machine */
    String getName();
    /**
     * Getter Function
     * 
     * @see EV3_Brick.initializeProperties()
     * @return The IP of the Machine */
    String getIP();
    /**
     * Getter Function
     * 
     * @see EV3_Brick.initializeProperties()
     * @return The MQTTSERV_IP of the Machine */
    String getMqttServerIP();
    /**
     * Getter Function
     * 
     * @see EV3_Brick.initializeProperties()
     * @return The REGCONF_TIMEOUT of the Machine */
    int getRegisterTimeout();
    /**
     * Getter Function
     * 
     * @see EV3_Brick.initializeProperties()
     * @return The TASKCONF_TIMEOUT of the Machine */
    int getTaskIndConfirmTimeout();
    /**
     * Getter Function
     * 
     * @see EV3_Brick.initializeProperties()
     * @return The TASKREQ_TIMEOUT of the Machine */
    int getTaskReqTimeout();
    /**
     * Getter Function
     * 
     * @see EV3_Brick.initializeProperties()
     * @return The SLEEP_TIME of the Machine */
    int getSleepTime();
    /**
     * Getter Function
     * 
     * @see EV3_Brick.initializeProperties()
     * @return The MAXMAINT_TIME of the Machine */
    int getMaxMaintTime();
}
