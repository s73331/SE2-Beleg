package monitoringtool;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Implementation of the model in the MVC pattern.
 * 
 * @author martin
 * 
 */
public class Model implements MqttModel, PSQLListener, Runnable {
    private static final Logger logger=LogManager.getLogger();
    private boolean debugMode=false;                                                //is debug mode enabled?
    private String recipes;
    private String deviceID;
    private String debugLog="";
    private ResultSet queryResult;
    private String currentQuery;                // IMPORTANT: currentQuery is saved as the statement, e.g. "SELECT * FROM ptime", not as "ptime-list"
    private PSQLHelper psql;
    private PropertyHelper propertyHelper;
    private String state="";
    private boolean dispatchActive=false;
    private MqttHelper mqtt;
    private String currentItem;
    private String failedItems;
    private String processedItems="";
    private String onlineTime="";
    private View view;
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
        psql=new PSQLHelper(propertyHelper.getHost(), propertyHelper.getPort(), propertyHelper.getDb(), propertyHelper.getUser(), propertyHelper.getPass(), this);
        logger.info("PSQLHelper created");
        mqtt=new MqttHelper(propertyHelper, deviceID, this);
        logger.debug("initialized Model");
    }
    public static Model getInstance() {
        return model;
    }
    public synchronized String getCurrentItem() {
        return currentItem;
    }
    public synchronized String getDebugLog() {
        return debugLog;
    }
    public synchronized String getDeviceID() {
        return deviceID;
        }
    public synchronized String getFailedItems() {
        return failedItems;
    }
    public int getHeight() {
        return propertyHelper.getHeight();          //won't get changed at any point, so no reason for sync
    }
    public synchronized String getOnlineTime() {
        return onlineTime;
    }
    public synchronized String getProcessedItems() {
        return processedItems;
    }
    public Collection<String> getQueries() {        //won't get changed at any point, so no reason for sync
        return propertyHelper.getQueries();
    }
    public synchronized ResultSet getQuery() {
        return queryResult;
    }
    public synchronized String getRecipes() {
        return recipes;
    }
    public synchronized String getState() {
        return state;
    }
    public int getWidth() {                          //won't get changed at any point, so no reason for sync
        return propertyHelper.getWidth();
    }
    public synchronized boolean hasMqttError() {
        return mqtt.hasError();
    }
    public synchronized boolean hasSQLError() {
        return psql.hasError();
    }
    @Override
    public synchronized boolean isDebugging() {
        return debugMode;
    }
    public synchronized boolean isDispatchActive() {
        return dispatchActive;
    }
    public synchronized boolean isMqttOnline() {
        return mqtt.isOnline();
    }
    public boolean isResultSetClosed() {
        try {
            return queryResult.isClosed();
        } catch (SQLException e) {
            logger.warn("SQLException when checking close status of queryResult");
            logger.debug("isResultSetClosed(): "+e);
            return true;
        }
    }
    public synchronized void setCurrentQuery(String name) {
        currentQuery=propertyHelper.getQuery(name);
        dispatchActive=false;
        logger.debug("setCurrentQuery(): not showing dispatch list");
        logger.info("new currentQuery: "+name);
    }
    public void setView(View view) {
        this.view=view;
    }
    public synchronized void toggleDebug() {
        debugMode^=mqtt.publish("debug "+!debugMode);
        logger.debug("toggleDebug(): debug mode toggled");
    }
    public synchronized String updateCurrentItem() {
        if("PROC".equals(state)) {
            currentItem=psql.getCurrentItem(deviceID);
        } else {
            logger.debug("not looking in db for current item, as machine is not in proc");
            currentItem="";
        }
        return currentItem;
    }
    public synchronized String updateFailedItems() {
        failedItems=psql.getFailedItems(deviceID);
        return failedItems;
    }
    public synchronized String updateOnlineTime() {
        if("DOWN".equals(state)) {
            logger.debug("getOnlineTime(): not looking for online time in db, as machine is down");
            onlineTime="";
        } else {
            onlineTime=psql.getOnlineTime(deviceID);
        }
        return onlineTime;
    }
    public synchronized String updateProcessedItems() {
        processedItems = psql.getProcessedItems(deviceID);
        return processedItems;
    }
    public synchronized ResultSet updateAutoQuery() {
        if(propertyHelper.shouldAutoUpdate(currentQuery)) return updateQuery();
        else {
            logger.info("not updating query, as it is not marked as autoupdate");
            return null;
        }
    }
    public synchronized ResultSet updateQuery() {
        try {
            logger.info("executing query: "+currentQuery);
            queryResult=psql.executeQuery(currentQuery);
            logger.info("updateQuery(): finished");
            return queryResult;
        } catch (SQLException sqle) {
            logger.error("SQLException when updating query, current query: "+currentQuery);
            logger.debug("updateQuery(): "+sqle);
            sqle.printStackTrace();
            return null;
        }
    }
    public synchronized String updateRecipes() {
        recipes=psql.getRecipes(deviceID);
        return recipes;
    }
    public synchronized String updateState() {
        if(mqtt.hasError()||!mqtt.isOnline()) {
            state=psql.getMachineState(deviceID);
            logger.info("new machine state: "+state);
            if("PROC".equals(state)||"MAINT".equals(state)||"IDLE".equals(state)||"DOWN".equals(state)||"UKN".equals(state)||"0".equals(state)) {
                //we are fine :)
            } else {
                if("".equals(state)) {
                    logger.error("no machine state string");
                } else {
                    logger.warn("illegal state string: "+state+", default-correcting to <empty>");
                    state="";
                }
            }
        }
        return state;
    }
    @Override
    public synchronized void addDebug(String debug) {
        debugLog+=debug;
        logger.debug("addDebug(): "+debug);
        if(!debugMode) {
            mqtt.publish("debug false");
        }
    }
    public void debugArrived() {
        if(view!=null) view.update();
    }
    public synchronized void emergencyShutdown() {
        if("DOWN".equals(state)) {
            logger.warn("machine down, not publishing emergency shutdown");
            return;
        }
        mqtt.publish("emergency shutdown");
    }
    public synchronized void fixMachine() {
        if("MAINT".equals(state)||"".equals(state)) {
            mqtt.publish("manual fix");
        } else {
            logger.warn("not in maint, not sending manual fix");
        }
    }
    @Override
    public synchronized void mqttConnectionLost() {
        if(view!=null) view.update();
    }
    @Override
    public synchronized void mqttConnected() {
        if(view!=null) view.update();
    }
    @Override
    public synchronized void newState(String state) {
        this.state=state;
        if(view!=null) view.update();
    }
    @Override
    public synchronized void psqlErrorFixed() {
        if(view!=null) view.update();
    }
    @Override
    public synchronized void psqlErrorOccured() {
        if(view!=null) view.update();
    }
    @Override
    public void run() {
        while(true) {
            updateState();
            updateCurrentItem();
            updateFailedItems();
            updateOnlineTime();
            updateProcessedItems();
            updateAutoQuery();
            updateRecipes();
            if(view!=null) view.update();
            try {
                logger.info("60s sleep");
                Thread.sleep(10000);
                logger.info("60s slept");
            } catch (InterruptedException ie) {
                logger.error("run(): InterruptedException: "+ie);
            }
        }
    }
    public synchronized void shutdown() {
        try {
            psql.close();
            mqtt.close();
        } catch (SQLException sqle) {
            logger.error("SQLException when closing SQLConnection");
            logger.debug("shutdown():\n"+sqle);
        }
    }
    public synchronized void fixMqtt() {
        mqtt.fix();
    }
}
