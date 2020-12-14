package com.holobor.helper;

import java.sql.*;

public class DatabaseHelper {

	private static DatabaseHelper sInstance = new DatabaseHelper();

	private Connection mConnection;
	private Statement mStatement;
	
	private DatabaseHelper() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		    mConnection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/vbrowser?user=root&password=jskk19931013&useSSL=false");
			mStatement = mConnection.createStatement();
		    // Do something with the Connection
		} catch (SQLException ex) {
		    // handle any errors
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static boolean executeSql(String sql) throws SQLException {
        return sInstance.mStatement.execute(sql);
    }

    public static ResultSet query(String sql) throws SQLException {
        return sInstance.mStatement.executeQuery(sql);
    }
}
