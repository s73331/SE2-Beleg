package monitoringtool.base;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import ev3utils.PSQLHelper;
import ev3utils.PropertyHelper;

/**
 * Implementation of the model in the MVC pattern.
 * 
 * @author martin
 * 
 */
public class Model {
    private boolean debugOpened;                                                    //is debug frame opened?
    private boolean debugMode;                                                      //is debug mode enabled?
    private boolean queryOpened;                                                    //is query frame opened?
    private String deviceID;                                          //TODO: load from properties
    private String[] recipes={"naps2", "flap2", "hop1"};                            //TODO
    private String lastRecipe="flap2";                                              //TODO
    private String currentItem="x205";                                              //TODO
    private String onlineTime="3h 24m 32s";                                         //TODO
    private int itemCount=33;                                                       //TODO
    private int failCount=2;                                                        //TODO
    private String debugLog="*Print Trace of Debug Messages*";                      //TODO
    private ResultSet queryResult;
    private String[] queryColumnNames={"uno", "dos", "tres"};                       //TODO
    private String[] queries;
    private String currentQuery="select * from PTIME where tool='ET1001';";         //TODO
    private PSQLHelper psql;
    private boolean psqlFailed=false;
    private PropertyHelper propertyHelper;
    private static Model model;
    static {
        try {
            model=new Model();
        } catch(IOException ioe) {
            ioe.printStackTrace();
            System.err.println("error: could not load properties");
            System.err.println("pwd: "+System.getProperty("user.dir"));
            System.err.println("expected file: monitoringtool.properties");
        }
    }
    private Model() throws IOException{
        propertyHelper=new PropertyHelper("monitoringtool.properties");
        try {
            psql=new PSQLHelper(propertyHelper.getHost(), propertyHelper.getPort(), propertyHelper.getDb(), propertyHelper.getUser(), propertyHelper.getPass());
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            psqlFailed=true;
        }
        deviceID=propertyHelper.getDeviceID();
        queries=propertyHelper.getQueries();
    }
    public static Model getInstance() {
        return model;
    }
    public void toggleDebugOpened() {
        debugOpened^=true;
    }
    public boolean isDebugOpened() {
        return debugOpened;
    }
    public boolean isDebugging() {
        return debugMode;
    }
    public String[] getRecipes() {
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
    public int getItemCount() {
        return itemCount;
    }
    public int getFailCount() {
        return failCount;
    }
    public void toggleDebug() {
        debugMode^=true;
    }
    public String getDebugLog() {
        return debugLog;
    }
    public boolean isQueriesOpened() {
        return queryOpened;
    }
    public void toggleQueriesOpened() {
        queryOpened^=true;
    }
    public ResultSet getQueryResult() {
        return queryResult;
    }
    public Object[] getColumnNames() {
        return queryColumnNames;
    }
    public String[] getQueries() {
        return queries;
    }
    public void updateQuery() {
        if(psqlFailed) return;
        //TODO
        try {
            queryResult=psql.executeQuery(currentQuery);
        } catch (SQLException e) {
            psqlFailed=true;
            e.printStackTrace();
            System.err.println("currentQuery: "+currentQuery);
        }
    }
    public String getDeviceID() {
        return deviceID;
    }
    public void shutdown() {
        if(psqlFailed) return;
        try {
            psql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void setCurrentQuery(String name) {
        currentQuery=propertyHelper.getQuery(name);
    }
}
