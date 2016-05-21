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
    private boolean sqlError;
    public PSQLHelper(String host, int port, String database, String user, String pass) {
        if(host==null) throw new IllegalArgumentException("host can not be null");
        if(port<1||port>65535) throw new IllegalArgumentException("illegal port number");
        if(database==null) throw new IllegalArgumentException("host can not be null");
        url="jdbc:postgresql://"+host+":"+port+"/"+database+"?user="+user+"&password="+pass;
        connect();
    }
    private boolean connect() {
        try {
            connection=DriverManager.getConnection(url);
            logger.info("database connection established");
            statement=connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            statement.setQueryTimeout(1);
            logger.debug("statement created");
            sqlError=false;
            return true;
        } catch (SQLException e) {
            logger.error("could not connect to database");
            sqlError=true;
            return false;
        }
    }
    public ResultSet executeQuery(String query) throws SQLException {
        if(sqlError&&!connect()) throw new SQLException("no connection to database");
        logger.info("executing query: "+query);
        return statement.executeQuery(query);
    }
    public String getMachineState(String deviceID) {
        ResultSet resultSet;
        String result="";
        try {
            resultSet = executeQuery("SELECT state FROM tool WHERE tool='"+deviceID+"';");
            resultSet.next();
            result=resultSet.getString("state");
        } catch (SQLException e) {
            logger.error("getMachineState: SQLException: "+e);;
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
            resultSet = statement.executeQuery(query);
            while(resultSet.next()) {
                if(resultSet.getString("recipe")!=null)result+=resultSet.getString("recipe")+",\n";
            }
        } catch (SQLException e) {
            logger.error("getRecipes: SQLException: "+e);;
        }
        if(result.length()>2) result=result.substring(0, result.length()-2);
        logger.info("recipes from psql: "+result.replace("\n", "\\n"));
        return result;
    }
    public void close() throws SQLException {
        connection.close();
    }
    public String getOnlineTime(String deviceID) {
        try {
            ResultSet rs=executeQuery("SELECT CURRENT_TIMESTAMP - (SELECT timestamp FROM events WHERE entity='"+deviceID+"' AND event LIKE '->%' AND timestamp > (SELECT timestamp FROM events WHERE entity='RTA1002' AND event='->DOWN' ORDER BY timestamp DESC LIMIT 1) ORDER BY timestamp ASC LIMIT 1) AS onlinetime;");
            rs.next();
            logger.info("onlinetime from psql: "+rs.getString("onlinetime"));
            return rs.getString("onlinetime");
        } catch(SQLException sqle) {
            logger.error("updateOnlineTime: SQLException "+sqle);
            return "";
        }
    }
    public String getCurrentItem(String deviceID) {
        try {
            ResultSet rs=executeQuery("SELECT note FROM events WHERE entity='"+deviceID+"' AND event='->PROC' ORDER BY TIMESTAMP DESC LIMIT 1;");
            rs.next();
            logger.info("current item from psql: "+rs.getString("note"));
            return rs.getString("note");
        } catch(SQLException sqle) {
            logger.error("updateCurrentItem: SQLException "+sqle);
            return "";
        }
    }
    public String getFailedItems(String deviceID) {
        try {
            ResultSet rs=executeQuery("SELECT COUNT(*) FROM events WHERE event='->MAINT' AND entity='"+deviceID+"' AND note LIKE '' GROUP BY event");
            rs.next();
            logger.info("failed items from psql: "+rs.getString("count"));
        return rs.getString("count");
        } catch(SQLException sqle) {
            logger.error("updateCurrentItem: SQLException "+sqle);
            return "";
        }
    }
    public String getProcessedItems(String deviceID) {
        try {
            ResultSet rs=executeQuery("SELECT COUNT(*) FROM events WHERE entity='"+deviceID+"' AND note='Processing finished' GROUP BY note;");
            rs.next();
            logger.info("processed items from psql: "+rs.getString("count"));
            return rs.getString("count");
        } catch (SQLException sqle) {
            logger.error("updateCurrentItem: SQLException "+sqle);
            return "";
        }
    }
}
