package monitoringtool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PSQLHelper {
    private static final Logger logger=LogManager.getLogger();
    private Connection connection;
    private Statement statement;
    private String url;
    private boolean error;
    private PSQLListener listener;
    public PSQLHelper(String host, int port, String database, String user, String pass, PSQLListener listener) {
        if(host==null) throw new IllegalArgumentException("host can not be null");
        if(port<1||port>65535) throw new IllegalArgumentException("illegal port number");
        if(database==null) throw new IllegalArgumentException("host can not be null");
        this.listener=listener;
        url="jdbc:postgresql://"+host+":"+port+"/"+database+"?user="+user+"&password="+pass;
        connect();
    }
    private void reportError() {
        boolean oldError=error;
        error=true;
        if(listener!=null&&!oldError) listener.psqlErrorOccured();
    }
    private void reportFix() {
        boolean oldError=error;
        error=false;
        if(listener!=null&&oldError) listener.psqlErrorFixed();
    }
    private boolean connect() {
        try {
            logger.debug("connect(): url: "+url);
            connection=DriverManager.getConnection(url);
            logger.info("database connection established");
            statement=connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            statement.setQueryTimeout(1);
            logger.debug("connect(): query timeout:"+statement.getQueryTimeout());
            logger.debug("connect(): statement created");
            reportFix();
            return true;
        } catch (SQLException sqle) {
            logger.error("could not connect to database");
            logger.debug("connect(): "+sqle);
            reportError();
            return false;
        }
    }
    public ResultSet executeQuery(String query) throws SQLException {
        if(error&&!connect()||query==null) return null;
        logger.info("executing query: "+query);
        return statement.executeQuery(query);
    }
    public String getMachineState(String deviceID) {
        ResultSet resultSet;
        String result="";
        try {
            resultSet = executeQuery("SELECT state FROM tool WHERE tool='"+deviceID+"';");
            if(resultSet==null) return result;
            if(resultSet.next()) {
                result=resultSet.getString("state");
            } else {
                logger.fatal("no tool "+deviceID);
                logger.debug("getMachineState(): ResultSet"+resultSet);
            }
        } catch (SQLException sqle) {
            logger.error("SQLException when getting machine state");
            logger.debug("getMachineState(): "+sqle);
            reportError();
        }
        logger.info("state from psql: "+result);
        return result;
    }
    public String getRecipes(String deviceID) {
        String query="SELECT recipe FROM ptime WHERE tool='"+deviceID+"';";
        logger.info(query);
        ResultSet resultSet;
        String result="";
        try {
            resultSet = executeQuery(query);
            if(resultSet==null) return "";
            while(resultSet.next()) {
                if(resultSet.getString("recipe")!=null)result+=resultSet.getString("recipe")+",\n";
            }
        } catch (SQLException sqle) {
            logger.error("SQLException when getting recipes");
            logger.debug("getRecipes(): "+sqle);
            reportError();
        }
        if(result.length()>2) result=result.substring(0, result.length()-2);
        logger.info("recipes from psql: "+result.replace("\n", "\\n"));
        return result;
    }
    public void close() throws SQLException {
        try {
            connection.close();
            logger.info("psql connection closed");
        } catch(NullPointerException npe) {
            logger.error("NullPointerException when closing psql connection");
            logger.debug("close(): "+npe);
            reportError();
        }
    }
    public String getOnlineTime(String deviceID) {
        try {
            ResultSet rs=executeQuery("SELECT CURRENT_TIMESTAMP - (SELECT timestamp FROM events WHERE entity='"+deviceID+"' AND event LIKE '->%' AND timestamp > (SELECT timestamp FROM events WHERE entity='"+deviceID+"' AND event='->DOWN' ORDER BY timestamp DESC LIMIT 1) ORDER BY timestamp ASC LIMIT 1) AS onlinetime;");
            if(rs==null) return "";
            if(rs.next()) {
                String result=rs.getString("onlineTime");
                if(result==null) result="";
                logger.info("onlinetime from psql: "+result);
                return result;
            } else {
                logger.warn("no online time");
                logger.debug("getOnlineTime(): ResultSet: "+rs);
                return "";
            }
        } catch(SQLException sqle) {
            logger.error("SQLException when getting online time");
            logger.debug("getOnlineTime(): "+sqle);
            reportError();
            return "";
        }
    }
    public String getCurrentItem(String deviceID) {
        try {
            ResultSet rs=executeQuery("SELECT note FROM events WHERE entity='"+deviceID+"' AND event='->PROC' ORDER BY TIMESTAMP DESC LIMIT 1;");
            if(rs==null) return "";
            if(rs.next()) {
                logger.info("current item from psql: "+rs.getString("note"));
                return rs.getString("note");
            } else {
                logger.warn("no current item");
                logger.debug("getCurrentItem(): ResultSet: "+rs);
                return "";
            }
        } catch(SQLException sqle) {
            logger.error("SQLException when getting current item");
            logger.debug("getCurrentItem(): "+sqle);
            reportError();
            return "";
        }
    }
    public String getFailedItems(String deviceID) {
        try {
            ResultSet rs=executeQuery("SELECT COUNT(*) FROM events24 WHERE event='->MAINT' AND entity='"+deviceID+"' AND note LIKE '' GROUP BY event;");
            if(rs==null) return "";
            if(rs.next()) {
                logger.info("failed items from psql: "+rs.getString("count"));
                return rs.getString("count");
            } else {
                logger.info("failed items from psql: 0");
                logger.debug("getFailedItems(): ResultSet: "+rs);
                return "0";
            }
        } catch(SQLException sqle) {
            logger.error("SQLException when getting failed items");
            logger.debug("getFailedItems(): "+sqle);
            reportError();
            return "";
        }
    }
    public String getProcessedItems(String deviceID) {
        try {
            ResultSet rs=executeQuery("SELECT COUNT(*) FROM events24 WHERE entity='"+deviceID+"' AND note='Processing finished' GROUP BY note;");
            if(rs==null) return "";
            if(rs.next()) {
            logger.info("processed items from psql: "+rs.getString("count"));
            return rs.getString("count");
            } else {
                logger.info("processed items from psql: 0");
                logger.debug("getProcessedItems(): ResultSet: "+rs);
                return "0";
            }
        } catch (SQLException sqle) {
            logger.error("SQLException when getting processed items");
            logger.debug("getProcessedItems(): "+sqle);
            reportError();
            return "";
        }
    }
    public boolean hasError() {
        return error;
    }
}
