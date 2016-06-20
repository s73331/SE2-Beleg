package ev3steuerung;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;

/**
 * Support class to handle properties file. Designed for use in ev3steuerung.
 * @author Martin Schöne */
 
public class PropertyHelper {
    Properties properties;
    
    /**
     * Class constructor of PropertyHelper Class
     * 
     * @param file - Filename of the .properties file to be loaded
     * @return PropertyHelper Class object
     * @throws IOException If there is anything wrong with the .properties file */
    protected PropertyHelper(String file) throws IOException {
        InputStream fileStream=new FileInputStream(file);
        properties=new Properties();
        properties.load(fileStream);
        
        //checks
        if(!properties.containsKey("name"))
            throw new IOException("key name not found");
        if(!properties.containsKey("ip"))
            throw new IOException("key ip not found");
        if(!properties.containsKey("mqttserverip"))
            throw new IOException("key mqttserverip not found");
        if(!properties.containsKey("registertimeout"))
            throw new IOException("key registertimeout not found");
        if(!properties.containsKey("taskindconfirmtimeout"))
            throw new IOException("key taskindconfirmtimeout not found");
        if(!properties.containsKey("taskreqtimeout"))
            throw new IOException("key taskreqtimeout not found");
        if(!properties.containsKey("sleeptime"))
            throw new IOException("key sleeptime not found");
        if(!properties.containsKey("maxmainttime"))
            throw new IOException("key maxmainttime not found");
        if(!isIntParsable(properties.getProperty("registertimeout")))
            throw new IOException("key registertimeout can not be parsed to int");
        if(!isIntParsable(properties.getProperty("taskindconfirmtimeout")))
            throw new IOException("key taskindconfirmtimeout can not be parsed to int");
        if(!isIntParsable(properties.getProperty("taskreqtimeout")))
            throw new IOException("key taskreqtimeout can not be parsed to int");
        if(!isIntParsable(properties.getProperty("sleeptime")))
            throw new IOException("key sleeptime can not be parsed to int");
        if(!isIntParsable(properties.getProperty("maxmainttime")))
            throw new IOException("key maxmainttime can not be parsed to int");
        if(null==InetAddress.getByName(properties.getProperty("ip")))
            throw new IOException("key ip does not match ip pattern");
        if(null==InetAddress.getByName(properties.getProperty("mqttserverip")))
            throw new IOException("key mqttserverip does not match ip pattern");
        
        fileStream.close();
    }
    private static boolean isIntParsable(String input){
        boolean parsable = true;
        try{
            Integer.parseInt(input);
        }catch(NumberFormatException e){
            parsable = false;
        }
        return parsable;
    }
    /**
     * Getter Function
     * 
     * @see EV3_Brick.initializeProperties()
     * @return The DEVICE_ID of the Machine */
    protected String getName() {
        return properties.getProperty("name");
    }
    /**
     * Getter Function
     * 
     * @see EV3_Brick.initializeProperties()
     * @return The IP of the Machine */
    protected String getIP() {
        return properties.getProperty("ip");
    }
    /**
     * Getter Function
     * 
     * @see EV3_Brick.initializeProperties()
     * @return The MQTTSERV_IP of the Machine */
    protected String getMqttServerIP() {
        return properties.getProperty("mqttserverip");
    }
    /**
     * Getter Function
     * 
     * @see EV3_Brick.initializeProperties()
     * @return The REGCONF_TIMEOUT of the Machine */
    protected int getRegisterTimeout() {
        return Integer.parseInt(properties.getProperty("registertimeout"));
    }
    /**
     * Getter Function
     * 
     * @see EV3_Brick.initializeProperties()
     * @return The TASKCONF_TIMEOUT of the Machine */
    protected int getTaskIndConfirmTimeout() {
        return Integer.parseInt(properties.getProperty("taskindconfirmtimeout"));
    }
    /**
     * Getter Function
     * 
     * @see EV3_Brick.initializeProperties()
     * @return The TASKREQ_TIMEOUT of the Machine */
    protected int getTaskReqTimeout() {
        return Integer.parseInt(properties.getProperty("taskreqtimeout"));
    }
    /**
     * Getter Function
     * 
     * @see EV3_Brick.initializeProperties()
     * @return The SLEEP_TIME of the Machine */
    protected int getSleepTime() {
        return Integer.parseInt(properties.getProperty("sleeptime"));
    }
    /**
     * Getter Function
     * 
     * @see EV3_Brick.initializeProperties()
     * @return The MAXMAINT_TIME of the Machine */
    protected int getMaxMaintTime() {
        return Integer.parseInt(properties.getProperty("maxmainttime"));
    }
}
