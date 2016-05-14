package base;

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
    private String deviceID="xswgfiwnf21";                                          //TODO: load from properties
    private String[] recipes={"naps2", "flap2", "hop1"};                            //TODO
    private String lastRecipe="flap2";                                              //TODO
    private String currentItem="x205";                                              //TODO
    private String onlineTime="3h 24m 32s";                                         //TODO
    private int itemCount=33;                                                       //TODO
    private int failCount=2;                                                        //TODO
    private String debugLog="*Print Trace of Debug Messages*";                      //TODO
    private String[][] queryResult={{"kek","kek2","kek3"},{"lel", "lel2", "lel3"}}; //TODO
    private String[] queryColumnNames={"uno", "dos", "tres"};                       //TODO
    private String[] queryChoices={"kek", "lel", "wut"};;                           //TODO
    private static Model model=new Model();
    private Model(){}
    public static Model getModel() {
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
    public Object[][] getQueryResult() {
        return queryResult;
    }
    public Object[] getColumnNames() {
        return queryColumnNames;
    }
    public String[] getQueryChoices() {
        return queryChoices;
    }
    public void updateQuery() {
        //TODO
    }
    public String getDeviceID() {
        return deviceID;
    }
}
