package monitoringtool;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Implementation of the model in the MVC pattern.
 * 
 * @author martin
 * 
 */
public class Model {
    private  static final Logger logger=LogManager.getRootLogger();
    private boolean debugMode=false;                                                //is debug mode enabled?
    private String deviceID;
    private String recipes;
    private String lastRecipe="flap2";                                              //TODO
    private String currentItem="x205";                                              //TODO
    private String onlineTime="3h 24m 32s";                                         //TODO
    private int itemCount=33;                                                       //TODO
    private int failCount=2;                                                        //TODO
    private String debugLog="";
    private ResultSet queryResult;
    private String[] queries;
    private String currentQuery;
    private PSQLHelper psql;
    private boolean psqlFailed=false;
    private PropertyHelper propertyHelper;
    private String machineState;
    private boolean mqttError=false;
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
        try {
            psql=new PSQLHelper(propertyHelper.getHost(), propertyHelper.getPort(), propertyHelper.getDb(), propertyHelper.getUser(), propertyHelper.getPass());
        } catch (SQLException sqle) {
            logger.error("could not initialize PSQLHelper: "+sqle);
            psqlFailed=true;
        }
        logger.debug("initialized Model");
    }
    public PSQLHelper getPSQLHelper() {
        return psql;
    }
    public static Model getInstance() {
        return model;
    }
    public boolean isDebugging() {
        return debugMode;
    }
    public String getRecipes() {
        return recipes;
    }
    public String getCurrentRecipe() {
        return lastRecipe;
    }
    public String getCurrentItem() {
        return currentItem;
    }
    public String getOnlineTime() {
        return onlineTime;
    }
    public int getProcessedItems() {
        return itemCount;
    }
    public int getFailedItems() {
        return failCount;
    }
    public void toggleDebug() {
        debugMode^=true;
        logger.debug("debug toggled");
    }
    public String getDebugLog() {
        return debugLog;
    }
    public ResultSet getQueryResult() {
        return queryResult;
    }
    public String[] getQueries() {
        return queries;
    }
    public void updateQuery() {
        if(psqlFailed) {
            logger.info("previous PSQLError, not updating query");
            return;
        }
        try {
            queryResult=psql.executeQuery(currentQuery);
            logger.info("updated query");
        } catch (SQLException e) {
            psqlFailed=true;
            logger.error("SQLException: "+e+"\ncurrent query: "+currentQuery);
        }
    }
    public String getDeviceID() {
        return deviceID;
    }
    public void shutdown() {
        try {
            psql.close();
        } catch (SQLException e) {
            logger.error("SQLException when closing SQLConnection");
        }
    }
    public void setCurrentQuery(String name) {
        currentQuery=propertyHelper.getQuery(name);
        logger.info("new currentQuery: "+name);
    }
    public int getHeight() {
        return propertyHelper.getHeight();
    }
    public int getWidth() {
        return propertyHelper.getWidth();
    }
    public void setSQLError(boolean b) {
        logger.info("psql error");
        psqlFailed=true;
    }
    public void setMqttError(boolean b) {
        mqttError=b;
    }
    public String getMqttServerURI() {
        return propertyHelper.getMqttServerURI();
    }
    public boolean hasMqttError() {
        return mqttError;
    }
    public void addDebug(String debug) {
        debugLog+=debug+"\n";
        logger.debug("debug added: "+debug);
    }
    public void setMachineState(String state) {
        if("PROC".equals(state)||"MAINT".equals(state)||"IDLE".equals(state)||"DOWN".equals(state))
        {
            machineState=state;
            logger.info("new machine state: "+state);
        } else {
            logger.warn("illegal state string");
            throw new IllegalArgumentException("illegal state string");
        }
    }
    public String getBackgroundColor() {
        switch(machineState) {
        case "DOWN":      return "aqua";
        case "PROC":      return "greenyellow";
        case "IDLE":      return "yellow";
        case "MAINT":     return "darksalmon";
        }
        return null;
    }
    public String getMachineState() {
        return machineState;
    }
    public void setRecipes(String recipes) {
        this.recipes=recipes;
    }
    public boolean hasSQLError() {
        return psqlFailed;
    }
}
