package monitoringtool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PSQLHelper {
    private Connection connection;
    private Statement statement;
    public PSQLHelper(String host, int port, String database, String user, String pass) throws SQLException {
        if(host==null) throw new IllegalArgumentException("host can not be null");
        if(port<1||port>65535) throw new IllegalArgumentException("illegal port number");
        if(database==null) throw new IllegalArgumentException("host can not be null");
        connection=DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/"+database+"?user="+user+"&password="+pass);
        statement=connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
    }
    public ResultSet executeQuery(String query) throws SQLException {
        return statement.executeQuery(query);
    }
    public void close() throws SQLException {
        connection.close();
    }
}
