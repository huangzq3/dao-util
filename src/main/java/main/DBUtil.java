package main;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by huangzhiqiang on 16/5/31.
 */
public class DBUtil {

    public static String url = "jdbc:mysql://localhost:3306/demo?";
    public static String user = "root";
    public static String password = "root";

    public static Connection getConnection(String url, String user, String password) throws SQLException {
        MysqlDataSource ds = new MysqlDataSource();
        ds.setUrl(url);
        ds.setUser(user);
        ds.setPassword(password);
        Connection connect = ds.getConnection();
        return connect;
    }

    public static ResultSet checkTable(Connection connect, String tableName) throws SQLException {
        DatabaseMetaData dmd = connect.getMetaData();
        ResultSet rs = dmd.getColumns(null, null, tableName, null);

        if (!rs.first()) {
            System.err.println("NONE DB OR TABLE!");
            throw new RuntimeException("NONE DB OR TABLE!");
        }
        return rs;
    }
}
