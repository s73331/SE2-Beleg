package monitoringtool;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PropertyHelper {
    private static final Logger logger=LogManager.getRootLogger();
    private Properties properties;
    Map<String, String> queries=new HashMap<String, String>();
    public PropertyHelper(String file) throws IOException {
        InputStream fileStream=new FileInputStream(file);
        properties=new Properties();
        properties.load(fileStream);
        //checks
        if(!properties.containsKey("host")) throw new IOException("key host not found");
        if(!properties.containsKey("port")) throw new IOException("key port not found");
        if(!properties.containsKey("db")) throw new IOException("key db not found");
        if(!properties.containsKey("user")) throw new IOException("key user not found");
        if(!properties.containsKey("pass")) throw new IOException("key pass not found");
        if(!properties.containsKey("deviceID")) throw new IOException("key deviceID not found");
        if(!properties.containsKey("height")) throw new IOException("key height not found");
        if(!properties.containsKey("width")) throw new IOException("key width not found");
        if(!properties.containsKey("mqttserveruri")) throw new IOException("key mqttserveruri not found");
        if(!isIntParsable(properties.getProperty("port"))) throw new IOException("key port can not be parsed to int");
        if(!isIntParsable(properties.getProperty("width"))) throw new IOException("key width can not be parsed to int");
        if(!isIntParsable(properties.getProperty("height"))) throw new IOException("key height can not be parsed to int");
        Set<String> allProperties=properties.stringPropertyNames();
        Iterator<String> queryIterator=allProperties.iterator();
        while(queryIterator.hasNext()) {
            String s=queryIterator.next();
            if(s.startsWith("query_")) {
                queries.put(s.substring(6).replace('_',' '), properties.getProperty(s));
            }
        }
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
    public Collection<String> getQueries() {
        return queries.keySet();
    }
    public int getHeight() {
        return Integer.parseInt(properties.getProperty("height"));
    }
    public int getWidth() {
        return Integer.parseInt(properties.getProperty("width"));
    }
    public String getMqttServerURI() {
        logger.debug("mqttserveruri: "+properties.getProperty("mqttserveruri"));
        return properties.getProperty("mqttserveruri");
    }
    public String getQuery(String name) {
        return queries.get(name);
    }
}
