package edu.jhu.ugrad.sseo6;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class SQLManager {
	private boolean dbExists;

	private String hostname = "";
	private String username = "";
	private String port = "";
	private String password = "";
	private String database = "";

	public void initialize(){
		try {
			dbExists = initializeCheck();
		} catch (SQLException e) {
			dbExists = false;
			System.out.println("WARNING: DBServer_Stats failed to connect to the database!");
			e.printStackTrace();
			return;
		} catch (IOException e) {
			dbExists = false;
			System.out.println("WARNING: DBServer_Stats failed to connect to the database!");
			e.printStackTrace();
			return;
		}
		
		if(!dbExists)
			System.out.println("WARNING: DBServer_Stats failed to connect to the database!");
	}
	
	private boolean initializeCheck() throws SQLException, IOException {
		
		FileInputStream fis = null;
		fis = new FileInputStream(DBServerMain.xml);
		try {
			XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(fis);
			while(xmlReader.hasNext())
			{
				xmlReader.next();
				if(xmlReader.isStartElement() && xmlReader.getLocalName().equals(DBServerMain.infoElement)){
					hostname = xmlReader.getAttributeValue(null, "hostname");
					username = xmlReader.getAttributeValue(null, "username");
					password = xmlReader.getAttributeValue(null, "password");
					port = xmlReader.getAttributeValue(null, "port");
					database = xmlReader.getAttributeValue(null, "database");
				}
			}
			
			xmlReader.close();
		} catch (XMLStreamException e) {
			e.printStackTrace();
			return false;
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
			return false;
		}

		if(password.equals(DBServerMain.defaultPassword) || database.equals(DBServerMain.defaultDatabase)
				|| username.equals(DBServerMain.defaultUsername)){
			System.out.println("ERROR: Default username, password, and/or database detected in DBSettings.xml!");
			System.out.println("  Please change the settings in the DBSettings.xml file.");
			return false;
		}
		
		Connection con = getConnection();
		
		if(con != null && con.isValid(10))
		{
			DBServerMain.instance().dataManager.initialize(con);
			con.close();
			return true;
		}

		return false;
	}
	
	public Connection getConnection(){
		Connection con = null;
		Properties cProps = new Properties();
		cProps.put("user", username);
		cProps.put("password", password);
		try {
			con = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database,cProps);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return con;
	}
	
	/**
	 * Updates the SQL Database with the query that is an
	 * insert/update/delete.
	 * If a connection is provided (is not null), then that connection
	 * will be used and will be preserved.
	 * @param query The SQL query that is executed.
	 * @param connection The provided connection, null if not provided.
	 */
	public void updateQuery(String query, Connection connection){
		boolean preserveConnection = false;
		Statement statement = null;
		Connection con = null;
		if(connection == null)
			con = DBServerMain.instance().sqlManager.getConnection();
		else
		{
			preserveConnection = true;
			con = connection;
		}
		try {
			statement = con.createStatement();
			statement.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		if(statement != null)
			try {
				statement.close();
			} catch (SQLException e) {}
		
		if(!preserveConnection && con != null)
			try {
				con.close();
			} catch (SQLException e) {}
	}
	
	/**
	 * Returns the result of the first row in the first column of the query.
	 * If a connection is provided (is not null), then that connection is used
	 * and will be preserved.
	 * @param query The SQL query that is executed.
	 * @param connection The provided connection, null if not provided.
	 * @return A String containing the result.
	 */
	public String standardQuery(String query, Connection connection){
		boolean preserveConnection = false;
		Statement statement = null;
		ResultSet results = null;
		Connection con = null;
		if(connection == null)
			con = DBServerMain.instance().sqlManager.getConnection();
		else
		{
			preserveConnection = true;
			con = connection;
		}
		String result = null;
		try {
			statement = con.createStatement();
			results = statement.executeQuery(query);
			results.next();
			result = results.getString(1);
		} catch (SQLException e) {}

		if(results != null)
			try {
				results.close();
			} catch (SQLException e) {}
		if(statement != null)
			try {
				statement.close();
			} catch (SQLException e) {}
		if(!preserveConnection && con != null)
			try {
				con.close();
			} catch (SQLException e) {}
		
		return result;
	}
/*
	public class SQLCallUpdate implements Runnable {
		private Statement statement = null;
		private String query;

		public SQLCallUpdate(String query) {
			this.query = query;
		}
		
		@Override
		public void run() {
			Connection con = DBServerMain.instance().sqlManager.getConnection();
			try {
				statement = con.createStatement();
				statement.executeUpdate(query);
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			
			if(statement != null)
				try {
					statement.close();
				} catch (SQLException e) {}
			
			if(con != null)
				try {
					con.close();
				} catch (SQLException e) {}
		}
	}*/
}
