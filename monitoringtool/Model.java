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
    /**
     * 
     * @return model instance
     */
    public static Model getInstance() {
        return model;
    }
    /**
     * Returns the name of the currently processed item, without updating it from the database.
     * @return Name of currently processed item
     */
    public synchronized String getCurrentItem() {
        return currentItem;
    }
    /**
     * 
     * @return all debug messages
     */
    public synchronized String getDebugLog() {
        return debugLog;
    }
    public synchronized String getDeviceID() {
        return deviceID;
    }
    /**
     * Returns the number of failed items in the last 24h, without updating it from the database.
     * @return number of failed items
     */
    public synchronized String getFailedItems() {
        return failedItems;
    }
    /**
     * Returns the preferred height from monitoringtool.properties.
     * @return preferred height in pixels
     */
    public int getHeight() {
        return propertyHelper.getHeight();          //won't get changed at any point, so no reason for sync
    }
    /**
     * Returns the current online time, without updating it from the database.
     * @return current online time
     */
    public synchronized String getOnlineTime() {
        return onlineTime;
    }
    /**
     * Returns the number of processed items in the last 24h, without updating it from the database.
     * @return number of processed items
     */
    public synchronized String getProcessedItems() {
        return processedItems;
    }
    //TODO
    public Collection<String> getQueries() {        //won't get changed at any point, so no reason for sync
        return propertyHelper.getQueries();
    }
    /**
     * Returns the ResultSet of the query, without updating it from the database.
     * @return current ResultSet
     */
    public synchronized ResultSet getQuery() {
        return queryResult;
    }
    /**
     * 
     * @return all recipes
     */
    public synchronized String getRecipes() {
        return recipes;
    }
    /**
     * 
     * @return current state
     */
    public synchronized String getState() {
        return state;
    }
    /**
     * Returns the preferred width from monitoringtool.properties.
     * @return preferred width in pixels
     */
    public int getWidth() {                          //won't get changed at any point, so no reason for sync
        return propertyHelper.getWidth();
    }
    /**
     * 
     * @return true - MQTT error, false - no MQTT error
     */
    public synchronized boolean hasMqttError() {
        return mqtt.hasError();
    }
    /**
     * 
     * @return true - SQL error, false - no SQL error
     */
    public synchronized boolean hasSQLError() {
        return psql.hasError();
    }
    /**
     * Returns, whether debug mode is activated.
     */
    @Override
    public synchronized boolean isDebugging() {
        return debugMode;
    }
    /**
     * Returns, whether the tool is connected via MQTT and the monitoring tool - EV3 protocol
     * @return
     */
    public synchronized boolean isMqttOnline() {
        return mqtt.isOnline();
    }
    /**
     * Returns, whether the current result set is closed
     * @return
     */
    public boolean isResultSetClosed() {
        try {
            return queryResult.isClosed();
        } catch (SQLException e) {
            logger.warn("SQLException when checking close status of queryResult");
            logger.debug("isResultSetClosed(): "+e);
            return true;
        }
    }
    /**
     * Sets a new query.
     * @param name name of the query
     */
    public synchronized void setCurrentQuery(String name) {
        currentQuery=propertyHelper.getQuery(name);
        logger.info("new currentQuery: "+name);
    }
    @Override
    public synchronized void setState(String state) {
        this.state=state;
        if(view!=null) view.update();
    }
    /**
     * Registers a view, to update it when needed.
     * @param view new view
     */
    public void setView(View view) {
        this.view=view;
    }
    /**
     * Toggles the debug mode.
     */
    public synchronized void toggleDebug() {
        debugMode^=mqtt.publish("debug "+!debugMode);
        logger.debug("toggleDebug(): debug mode toggled");
    }
    /**
     * Tries to retrieve the currently processed item from the database.
     * @return empty string - machine is not in PROC, else name of the currently processed item
     */
    public synchronized String updateCurrentItem() {
        if("PROC".equals(state)) {
            currentItem=psql.getCurrentItem(deviceID);
        } else {
            logger.debug("not looking in db for current item, as machine is not in proc");
            currentItem="";
        }
        return currentItem;
    }
    /**
     * Updates the number of failed items and returns them.
     * @return number of failed items.
     */
    public synchronized String updateFailedItems() {
        failedItems=psql.getFailedItems(deviceID);
        return failedItems;
    }
    /**
     * Updates the online time and returns it.
     * @return online time
     */
    public synchronized String updateOnlineTime() {
        if("DOWN".equals(state)) {
            logger.debug("getOnlineTime(): not looking for online time in db, as machine is down");
            onlineTime="";
        } else {
            onlineTime=psql.getOnlineTime(deviceID);
        }
        return onlineTime;
    }
    /**
     * Updates the number of processed items and returns them.
     * @return number of processed items.
     */
    public synchronized String updateProcessedItems() {
        processedItems = psql.getProcessedItems(deviceID);
        return processedItems;
    }
    /**
     * Updates the ResultSet, if the query is marked as autoupdating.
     * @return null - query not marked as autoupdating, else updated ResultSet
     */
    public synchronized ResultSet updateAutoQuery() {
        if(propertyHelper.shouldAutoUpdate(currentQuery)) return updateQuery();
        else {
            logger.info("not updating query, as it is not marked as autoupdate");
            return null;
        }
    }
    /**
     * Updates the query.
     * @return updated ResultSet
     */
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
    /**
     * Updates the list of recipes.
     * @return all recipes
     */
    public synchronized String updateRecipes() {
        recipes=psql.getRecipes(deviceID);
        return recipes;
    }
    /**
     * Retrieves the state from the database, when MQTT has an error or the tool is not connected.
     * @return machine state
     */
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
    public void debugArrived(String debug) {
        debugLog+=debug;
        logger.debug("addDebug(): "+debug);
        if(!debugMode) {
            mqtt.publish("debug false");
        }
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
    public synchronized void psqlErrorFixed() {
        if(view!=null) view.update();
    }
    @Override
    public synchronized void psqlErrorOccured() {
        if(view!=null) view.update();
    }
    /**
     * Updates all information every 60s.
     */
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
                Thread.sleep(60000);
                logger.info("60s slept");
            } catch (InterruptedException ie) {
                logger.error("run(): InterruptedException: "+ie);
            }
        }
    }
    /**
     * Closes PSQLHelper and MqttHelper.
     */
    public synchronized void shutdown() {
        try {
            psql.close();
            mqtt.close();
        } catch (SQLException sqle) {
            logger.error("SQLException when closing SQLConnection");
            logger.debug("shutdown():\n"+sqle);
        }
    }
    /**
     * Attempts to fix MQTT connection.
     */
    public synchronized void fixMqtt() {
        mqtt.fix();
    }
}
