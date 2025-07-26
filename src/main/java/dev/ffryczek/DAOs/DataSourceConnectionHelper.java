package dev.ffryczek.DAOs;


import java.sql.Connection;
import java.sql.SQLException;

import com.mysql.cj.jdbc.MysqlDataSource;


public class DataSourceConnectionHelper {

    private MysqlDataSource dataSource;
    private final String defaultURL = "jdbc:mysql://localhost:3306/Drom8DB";
    private final String defaultPASSWORD = "fici=me5ma";
    private final String defaultUSER = "root";

    //Setup of connection parameters 
    public void connectCredentials(String url, String user, String password) {
        dataSource = new MysqlDataSource();
        dataSource.setURL(url);
        dataSource.setUser(user);
        dataSource.setPassword(password);

    }

    //Setup of connection parameters 
    public void connectCredentials() {
        dataSource = new MysqlDataSource();
        dataSource.setURL(defaultURL);
        dataSource.setUser(defaultUSER);
        dataSource.setPassword(defaultPASSWORD);

    }

    //Connection object single function
    public Connection openConnection() throws SQLException {
        return dataSource.getConnection();
    }
    //dataSource object getter
    public MysqlDataSource getDataSource() {
        return dataSource;
    }
}
