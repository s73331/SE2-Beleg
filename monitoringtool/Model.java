package monitoringtool;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


/**
 * Implementation of the model in the MVC pattern.
 * 
 * @author martin
 * 
 */
public class Model implements MqttCallback {
    private static final Logger logger=LogManager.getRootLogger();
    private boolean debugMode=false;                                                //is debug mode enabled?
    private String deviceID;
    private String debugLog="";
    private ResultSet queryResult;
    private String[] queries;
    private String currentQuery;
    private PSQLHelper psql;
    private PropertyHelper propertyHelper;
    private String machineState="DOWN";
    private boolean mqttError=false;
    private MqttAsyncClient mqtt;
    private MqttMiniCallback callback;
    private static Model model;
    static {
        model=new Model();
    }
    private Model() {
        try {
            propertyHelper=new PropertyHelper("monitoringtool.properties");
        } catch (IOException e) {
            logger.fatal("error: could not load properties\ncwd: "+System.getProperty("user.dir")+"\nexpected file: monitoringtool.properties");
            System.exit(1);
        }
        deviceID=propertyHelper.getDeviceID();
        queries=propertyHelper.getQueries();
        psql=new PSQLHelper(propertyHelper.getHost(), propertyHelper.getPort(), propertyHelper.getDb(), propertyHelper.getUser(), propertyHelper.getPass());
        logger.info("PSQLHelper created");
        try {
            logger.debug("kek");
            mqtt=new MqttAsyncClient(propertyHelper.getMqttServerURI(), MqttAsyncClient.generateClientId()); //todo: ClientID
            logger.debug("MqttClient constructed");
            mqtt.setCallback(this);
            IMqttToken conToken = mqtt.connect();
            conToken.waitForCompletion();
            logger.debug("MqttClient connected");
            IMqttToken subToken = mqtt.subscribe(deviceID,0);  //Qos 0?
            subToken.waitForCompletion();
            logger.info("subscribed to mqtt topic "+deviceID);

        } catch (MqttException mqtte) {
            logger.error("MqttException: "+mqtte);
            mqttError=true;
        } catch (IllegalArgumentException iae) {
            logger.error("IllegalArgumentException: "+iae);
            mqttError=true;
        } catch(NullPointerException npe) {
            logger.error("NullPointerException: "+npe);
            mqttError=true;
        }
        logger.debug("initialized Model");
    }
    public static Model getInstance() {
        return model;
    }
    public synchronized boolean isDebugging() {
        return debugMode;
    }
    public synchronized void toggleDebug() {
        if(mqttError) return;
        debugMode^=publish("debug "+!debugMode);
    }
    public synchronized String getDebugLog() {
        return debugLog;
    }
    public synchronized String[] getQueries() {
        return queries;
    }
    public synchronized ResultSet updateQuery() {
        try {
            queryResult=psql.executeQuery(currentQuery);
            logger.info("updated query");
            return queryResult;
        } catch (SQLException e) {
            logger.error("SQLException: "+e+"\ncurrent query: "+currentQuery);
            return null;
        }
    }
    public synchronized String getDeviceID() {
        return deviceID;
    }
    public synchronized void shutdown() {
        try {
            psql.close();
        } catch (SQLException e) {
            logger.error("SQLException when closing SQLConnection");
        }
    }
    public synchronized void setCurrentQuery(String name) {
        currentQuery=propertyHelper.getQuery(name);
        logger.info("new currentQuery: "+name);
    }
    public synchronized int getHeight() {
        return propertyHelper.getHeight();
    }
    public synchronized int getWidth() {
        return propertyHelper.getWidth();
    }
    public synchronized boolean hasMqttError() {
        return mqttError;
    }
    public synchronized void addDebug(String debug) {
        debugLog+=debug+"\n";
        logger.debug("debug added: "+debug);
    }
    public synchronized String getBackgroundColor() {
        switch(machineState) {
        case "DOWN":      return "aqua";
        case "PROC":      return "greenyellow";
        case "IDLE":      return "yellow";
        case "MAINT":     return "darksalmon";
        }
        return null;
    }
    public synchronized String getMachineState() {
        String state=psql.getMachineState(deviceID);
        if("PROC".equals(state)||"MAINT".equals(state)||"IDLE".equals(state)||"DOWN".equals(state)) {
            machineState=state;
            logger.info("new machine state: "+state);
        } else {
            logger.warn("illegal state string");
            throw new IllegalArgumentException("illegal state string");
        }
        return machineState;
    }
    public synchronized String getRecipes() {
        return psql.getRecipes(deviceID);
    }
    @Override
    public synchronized void connectionLost(Throwable arg0) {
        logger.error("mqtt connection lost");
        mqttError=true;
        callback.connectionLost();
    }
    @Override
    public synchronized void deliveryComplete(IMqttDeliveryToken token) {
        try {
            logger.debug("delivery complete: "+token.getMessage());
        } catch (MqttException e) {
            logger.warn("delivery complete, MqttException: "+e);
        }
    }
    @Override
    public synchronized void messageArrived(String topic, MqttMessage message) throws Exception {
        logger.info("message arrived on "+topic+": "+message);
        String[] information=new String(message.getPayload()).split(" ");
        if(information.length==2) {
            if("emergency".equals(information[0])&&"shutdown".equals(information[1])) return;
            if("manual".equals(information[0])&&"fix".equals(information[1])) return;
        }
        if(information.length==1&&"hello".equals(information[0])) {
            publish("debug "+debugMode);
            return;
        }
        if("debug".equals(information[0])) {
            if(information.length==2&&("true".equals(information[1])||"false".equals(information[1]))) return;
            if(information.length>1) {
                String debug=new String(message.getPayload()).substring("debug ".length());
                debugLog+=debug+"\n";
                logger.debug("debug added: "+debug);
                callback.debugArrived();
                return;
            }
        }
        logger.warn("undefined message"+information);
    }
    public synchronized void setMqttCallback(MqttMiniCallback callback) {
        this.callback=callback;
    }
    public synchronized boolean publish(String message) {
        if(mqttError) {
            logger.warn("MqttError, not publishing: "+message);
            return false;
        }
        try {
            mqtt.publish(deviceID, new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            logger.error("MqttException: "+e);
            mqttError=true;
            return false;
        }
        logger.info("published message to "+deviceID+": "+message);
        return true;
    }
    public synchronized boolean fixMqtt() {
        if(model.hasMqttError()) {
            try {
                IMqttToken conToken = mqtt.connect();
                conToken.waitForCompletion();
                logger.info("MqttClient reconnected");
                mqttError=false;
            } catch (MqttException e) {
                logger.error("MqttException: "+e);
                mqttError=true;
                return false;
            }
        }
        return true;
    }
    public synchronized void emergencyShutdown() {
        if(mqttError) {
            logger.info("mqttError, not publishing emergency shutdown");
            return;
        }
        if("DOWN".equals(machineState)) {
            logger.info("machine down, not publishing emergency shutdown");
            return;
        }
        publish("emergency shutdown");
    }
    public synchronized void fixMachine() {
        if("MAINT".equals(model.getMachineState())) {
            model.publish("manual fix");
        } else {
            logger.info("not in maint, not sending manual fix");
        }
    }
    public synchronized String getOnlineTime() {
        return psql.getOnlineTime(deviceID);
    }
    public synchronized String getCurrentItem() {
        return psql.getCurrentItem(deviceID);
    }
    public synchronized String getFailedItems() {
        return psql.getFailedItems(deviceID);
    }
    public synchronized String getProcessedItems() {
        return psql.getProcessedItems(deviceID);
    }
}
