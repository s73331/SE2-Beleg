package monitoringtool;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyHelper {
    private Properties properties;
    public PropertyHelper(String file) throws IOException {
        InputStream fileStream=new FileInputStream(file);
        properties=new Properties();
        properties.load(fileStream);
        //checks
        if(!properties.containsKey("host")) throw new IOException("key host not found");
        if(!properties.containsKey("port")) throw new IOException("key port not found");
        if(!isIntParsable(properties.getProperty("port"))) throw new IOException("key port can not be parsed to int");
        if(!properties.containsKey("db")) throw new IOException("key db not found");
        if(!properties.containsKey("user")) throw new IOException("key user not found");
        if(!properties.containsKey("pass")) throw new IOException("key pass not found");
        if(!properties.containsKey("deviceID")) throw new IOException("key deviceID not found");
        if(!properties.containsKey("queries")) throw new IOException("key queries not found");
        for(String query : getQueries()) {
            if(!properties.containsKey(query)) throw new IOException("key "+query+" not found");
        }
        if(!properties.containsKey("height")) throw new IOException("key height not found");
        if(!isIntParsable(properties.getProperty("height"))) throw new IOException("key height can not be parsed to int");
        if(!properties.containsKey("width")) throw new IOException("key width not found");
        if(!isIntParsable(properties.getProperty("width"))) throw new IOException("key width can not be parsed to int");
        if(!properties.containsKey("mqttserveruri")) throw new IOException("key mqttserveruri not found");
        
    }
    public static boolean isIntParsable(String input){
        boolean parsable = true;
        try{
            Integer.parseInt(input);
        }catch(NumberFormatException e){
            parsable = false;
        }
        return parsable;
    }
    public String getHost() {
        return properties.getProperty("host");
    }
    public int getPort() {
        return Integer.parseInt(properties.getProperty("port"));
    }
    public String getDb() {
        return properties.getProperty("db");
    }
    public String getUser() {
        return properties.getProperty("user");
    }
    public String getPass() {
        return properties.getProperty("pass");
    }
    public String getDeviceID() {
        return properties.getProperty("deviceID");
    }
    public String[] getQueries() {
        return properties.getProperty("queries").split(":");
    }
    public String getQuery(String name) {
        return properties.getProperty(name);
    }
    public int getHeight() {
        return Integer.parseInt(properties.getProperty("height"));
    }
    public int getWidth() {
        return Integer.parseInt(properties.getProperty("width"));
    }
    public String getMqttServerURI() {
        return properties.getProperty("mqttserveruri");
    }
}
