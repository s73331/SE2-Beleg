package ev3steuerung;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;

public class PropertyHelper {
    Properties properties;
    public PropertyHelper(String file) throws IOException {
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
    public String getName() {
        return properties.getProperty("name");
    }
    public String getIP() {
        return properties.getProperty("ip");
    }
    public String getMqttServerIP() {
        return properties.getProperty("mqttserverip");
    }
    public String getMesIP() {
        return properties.getProperty("mesip");
    }
    public int getRegisterTimeout() {
        return Integer.parseInt(properties.getProperty("registertimeout"));
    }
    public int getTaskIndConfirmTimeout() {
        return Integer.parseInt(properties.getProperty("taskindconfirmtimeout"));
    }
    public int getTaskReqTimeout() {
        return Integer.parseInt(properties.getProperty("taskreqtimeout"));
    }
    public int getSleepTime() {
        return Integer.parseInt(properties.getProperty("sleeptime"));
    }
    public int getMaxMaintTime() {
        return Integer.parseInt(properties.getProperty("maxmainttime"));
    }
}
