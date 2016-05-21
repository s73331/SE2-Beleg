package monitoringtool;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Implementation of the model in the MVC pattern.
 * 
 * @author martin
 * 
 */
public class Model implements MqttMiniCallback, PSQLListener {
    private static final Logger logger=LogManager.getRootLogger();
    private boolean debugMode=false;                                                //is debug mode enabled?
    private String deviceID;
    private String debugLog="";
    private ResultSet queryResult;
    private Set<String> queries;
    private String currentQuery;
    private PSQLHelper psql;
    private PropertyHelper propertyHelper;
    private String state="";
    private boolean mqttError=false;
    private MqttMiniCallback mqttCallback;
    private boolean dispatchActive=false;
    private MqttHelper mqttHelper;
    private PSQLListener psqlListener;
    private static Model model=new Model();
    private Model() {
        try {
            propertyHelper=new PropertyHelper("monitoringtool.properties");
        } catch (IOException ioe) {
            logger.fatal("error: could not load properties\ncwd: "+System.getProperty("user.dir")+"\nexpected file: monitoringtool.properties\n"+ioe);
            logger.debug("Model(): \n"+ioe);
            System.exit(1);
        }
        deviceID=propertyHelper.getDeviceID();
        queries=propertyHelper.getQueries();
        if(!queries.add("Dispatchliste")){
            logger.warn("ignoring users query \"Dispatchliste\"");
        }
        psql=new PSQLHelper(propertyHelper.getHost(), propertyHelper.getPort(), propertyHelper.getDb(), propertyHelper.getUser(), propertyHelper.getPass(), this);
        logger.info("PSQLHelper created");
        mqttHelper=new MqttHelper(propertyHelper, deviceID, this);
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
        logger.debug("toggleDebug(): debug mode toggled");
    }
    public synchronized String getDebugLog() {
        return debugLog;
    }
    public synchronized Collection<String> getQueries() {
        return queries;
    }
    public synchronized ResultSet updateQuery() {
        try {
            queryResult=psql.executeQuery(currentQuery);
            logger.info("updateQuery(): finished");
            return queryResult;
        } catch (SQLException sqle) {
            logger.error("SQLException when updating query, current query: "+currentQuery);
            logger.debug("updateQuery():\n"+sqle);
            return null;
        }
    }
    public synchronized String getDeviceID() {
        return deviceID;
    }
    public synchronized void shutdown() {
        try {
            psql.close();
        } catch (SQLException sqle) {
            logger.error("SQLException when closing SQLConnection");
            logger.debug("shutdown():\n"+sqle);
        }
    }
    public synchronized void setCurrentQuery(String name) {
        if("Dispatchliste".equals(name)) {
            currentQuery="SELECT * FROM lot WHERE disptool='"+deviceID+"';";
            dispatchActive=true;
            logger.debug("setCurrentQuery(): showing dispatch list");
        } else {
            currentQuery=propertyHelper.getQuery(name);
            dispatchActive=false;
            logger.debug("setCurrentQuery(): not showing dispatch list");
        }
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
        logger.debug("addDebug(): "+debug);
    }
    public synchronized String getMachineState() {
        state=psql.getMachineState(deviceID);
        logger.info("new machine state: "+state);
        if("PROC".equals(state)||"MAINT".equals(state)||"IDLE".equals(state)||"DOWN".equals(state)) {
            return state;
        } else {
            if("".equals(state)) {
                logger.error("no machine state string");
                return state;
            } else {
                logger.warn("illegal state string: "+state+", default-correcting to <empty>");
                state="";
                return "";
            }
        }
    }
    public synchronized String getRecipes() {
        return psql.getRecipes(deviceID);
    }

    public synchronized void setMqttCallback(MqttMiniCallback callback) {
        this.mqttCallback=callback;
        logger.debug("setMqttCallback(): "+callback);
    }
    public synchronized void emergencyShutdown() {
        if(mqttError) {
            logger.warn("mqttError, not publishing emergency shutdown");
            return;
        }
        if("DOWN".equals(state)) {
            logger.warn("machine down, not publishing emergency shutdown");
            return;
        }
        publish("emergency shutdown");
    }
    public synchronized void fixMachine() {
        if("MAINT".equals(model.getMachineState())) {
            publish("manual fix");
        } else {
            logger.warn("not in maint, not sending manual fix");
        }
    }
    public synchronized String getOnlineTime() {
        if("DOWN".equals(psql.getMachineState(deviceID))) {
            logger.debug("getOnlineTime(): not looking for online time in db, as machine is down");
            return "DOWN";
        }
        return psql.getOnlineTime(deviceID);
    }
    public synchronized String getCurrentItem() {
        if("PROC".equals(psql.getMachineState(deviceID))) {
        return psql.getCurrentItem(deviceID);
        } else {
            logger.debug("not looking in db for current item, as machine is not in proc");
            return "";
        }
    }
    public synchronized String getFailedItems() {
        return psql.getFailedItems(deviceID);
    }
    public synchronized String getProcessedItems() {
        return psql.getProcessedItems(deviceID);
    }
    public synchronized boolean isDispatchActive() {
        return dispatchActive;
    }
    public boolean publish(String message) {
        return mqttHelper.publish(message);
    }
    public void connectionLost() {
        if(mqttCallback!=null) mqttCallback.connectionLost();
    }
    public void debugArrived() {
        if(mqttCallback!=null) mqttCallback.debugArrived();
    }
    public void connected() {
        if(mqttCallback!=null) mqttCallback.connected();
    }
    public void mqttFix() {
        mqttHelper.fix();
    }
    @Override
    public void errorOccured() {
        if(psqlListener!=null) psqlListener.errorOccured();
    }
    @Override
    public void errorFixed() {
        if(psqlListener!=null) psqlListener.errorFixed();
    }
    public void setPSQLListener(PSQLListener listener) {
        psqlListener=listener;
    }
}
