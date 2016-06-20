package monitoringtool;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Support class to handle properties file. Designed for use in monitoringtool.Model.
 * @author martin
 *
 */
public class PropertyHelper {
    private static final Logger logger=LogManager.getLogger();
    private Properties properties;
    Map<String, String> queries=new HashMap<String, String>();
    Set<String> autoUpdateQueries=new HashSet<String>();
    /**
     * 
     * @param file path to the properties file
     * @throws IOException When the file could not be opened or does not have necessary keys.
     */
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
            if(s.contains("query")) {
                String query=properties.getProperty(s).replace("<tool>", getDeviceID());
                if(s.contains("autoupdate")) {
                    autoUpdateQueries.add(query);
                }
                queries.put(s.replace("query", "").replace('_',' ').replace("autoupdate", ""), query);
            }
        }
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
     * 
     * @return IP of the database
     */
    public String getHost() {
        return properties.getProperty("host");
    }
    /**
     * 
     * @return port of the database
     */
    public int getPort() {
        return Integer.parseInt(properties.getProperty("port"));
    }
    /**
     * 
     * @return database name
     */
    public String getDb() {
        return properties.getProperty("db");
    }
    /**
     * 
     * @return username to be used for the database
     */
    public String getUser() {
        return properties.getProperty("user");
    }
    /**
     * 
     * @return passphrase to be used for the database
     */
    public String getPass() {
        return properties.getProperty("pass");
    }
    /**
     * 
     * @return device ID
     */
    public String getDeviceID() {
        return properties.getProperty("deviceID");
    }
    /**
     * 
     * @return Set of the names of the queries
     */
    public Set<String> getQueries() {
        return new HashSet<String>(queries.keySet()); //HashSet supports the add(String) method as opposed to the keySet of a HashMap
    }
    /**
     * 
     * @return preferred window height
     */
    public int getHeight() {
        return Integer.parseInt(properties.getProperty("height"));
    }
    /**
     * 
     * @return preferred window width
     */
    public int getWidth() {
        return Integer.parseInt(properties.getProperty("width"));
    }
    /**
     * 
     * @return URI of the MQTT broker
     */
    public String getMqttServerURI() {
        logger.debug("mqttserveruri: "+properties.getProperty("mqttserveruri"));
        return properties.getProperty("mqttserveruri");
    }
    /**
     * Returns a SQL query defined by it's name.
     * @param name
     * @return SQL query
     */
    public String getQuery(String name) {
        return queries.get(name);
    }
    /**
     * Returns, whether this query should be updated every 60s.
     * @param name
     * @return
     */
    public boolean shouldAutoUpdate(String name) {
        return autoUpdateQueries.contains(name);
    }
}
