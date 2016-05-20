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
    public PSQLHelper(Model model, String host, int port, String database, String user, String pass) throws SQLException {
        if(host==null) throw new IllegalArgumentException("host can not be null");
        if(model==null) throw new IllegalArgumentException("model can not be null");
        if(port<1||port>65535) throw new IllegalArgumentException("illegal port number");
        if(database==null) throw new IllegalArgumentException("host can not be null");
        connection=DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/"+database+"?user="+user+"&password="+pass);
        logger.info("database connection established");
        statement=connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
    }
    public ResultSet executeQuery(String query) throws SQLException {
        logger.info("executing query: "+query);
        return statement.executeQuery(query);
    }
    public void close() throws SQLException {
        connection.close();
    }
    public String getMachineState(String deviceID) throws SQLException {
        ResultSet resultSet=statement.executeQuery("select event from events24 where entity='"+deviceID+"' and event like '%.Period' order by timestamp desc limit 1;");
        resultSet.next();
        String result=resultSet.getString("event");
        result=result.substring(0,result.indexOf("."));
        logger.info("state from psql: "+result);
        return result;
    }
}
