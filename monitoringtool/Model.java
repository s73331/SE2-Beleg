package monitoringtool;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Implementation of the model in the MVC pattern.
 * 
 * @author martin
 * 
 */
public class Model {
    private boolean debugMode=false;                                                //is debug mode enabled?
    private String deviceID;
    private String[] recipes={"naps2", "flap2", "hop1"};                            //TODO
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
    private boolean mqttError=false;
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
    public boolean isDebugging() {
        return debugMode;
    }
    public String[] getRecipes() {
        return recipes;
    }
    public String getRecipesString() {
        String ret="";
        for(String s:recipes) {
            ret+=s+", ";
        }
        return ret.substring(0, ret.length()-2);
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
        if(psqlFailed) return;
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
    public int getHeight() {
        return propertyHelper.getHeight();
    }
    public int getWidth() {
        return propertyHelper.getWidth();
    }
    public void setSQLError(boolean b) {
        psqlFailed=true;        
    }
    public void setMqttError(boolean b) {
        mqttError=true;
    }
    public String getMqttServerURI() {
        return propertyHelper.getMqttServerURI();
    }
    public boolean hasMqttError() {
        return mqttError;
    }
    public void addDebug(String debug) {
        debugLog+=debug+"\n";
    }
}
