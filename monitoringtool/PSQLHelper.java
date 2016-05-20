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
    String url;
    public PSQLHelper(String host, int port, String database, String user, String pass) throws SQLException {
        if(host==null) throw new IllegalArgumentException("host can not be null");
        if(port<1||port>65535) throw new IllegalArgumentException("illegal port number");
        if(database==null) throw new IllegalArgumentException("host can not be null");
        url="jdbc:postgresql://"+host+":"+port+"/"+database+"?user="+user+"&password="+pass;
        connection=DriverManager.getConnection(url);
        logger.info("database connection established");
        statement=connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
    }
    public boolean renewConnection() {
        try {
            connection=DriverManager.getConnection(url);
            logger.info("database connection established");
            statement=connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            return true;
        } catch (SQLException e) {
            logger.info("could not renew connection: "+e);
        }
        return false;
    }
    public ResultSet executeQuery(String query) throws SQLException {
        logger.info("executing query: "+query);
        return statement.executeQuery(query);
    }
    public void close() throws SQLException {
        connection.close();
    }
    public String getMachineState(String deviceID) {
        String query="select event from events where entity='"+deviceID+"' and event like '->%' order by timestamp desc limit 1;";
        logger.info(query);
        ResultSet resultSet;
        String result="";
        try {
            resultSet = statement.executeQuery(query);
            resultSet.next();
            result=resultSet.getString("event");
        } catch (SQLException e) {
            logger.error("getMachineState: SQLException: "+e);;
        }
        result=result.substring(2);
        logger.info("state from psql: "+result);
        return result;
    }
    public String getRecipes(String deviceID) {
        String query="select recipe from ptime where tool='"+deviceID+"';";
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
}
