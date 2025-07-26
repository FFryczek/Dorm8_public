package dev.ffryczek;

import java.sql.DriverManager;
import java.util.Enumeration;

import dev.ffryczek.DAOs.DataSourceConnectionHelper;

import java.sql.Connection;
import java.sql.Driver;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        // JDBC Driver load
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements() == true) {
                System.out.println(drivers.nextElement().getClass().getName());
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Driver not found!");
        }

        //Connection connection = DatabaseConnector.establishConnection();


    String url = "jdbc:mysql://localhost:3306/TestDatabase";
    String password = "fici=me5ma";
    String user = "root";
    DataSourceConnectionHelper dataSource = new DataSourceConnectionHelper();
    dataSource.connectCredentials(url, user, password);

    }
}
