package edu.jhu.ugrad.sseo6;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class SQLManager {
	private boolean dbExists = false;

	private String hostname = "";
	private String username = "";
	private String port = "";
	private String password = "";
	private String database = "";

	public void initialize(){
		try {
			initializeCheck();
		} catch (SQLException e) {
			dbExists = false;
			System.out.println("ERROR: DBServer_Stats failed to connect to the database!");
			e.printStackTrace();
			return;
		} catch (IOException e) {
			dbExists = false;
			System.out.println("ERROR: DBServer_Stats failed to connect to the database!");
			e.printStackTrace();
			return;
		}
		
		if(!dbExists)
			System.out.println("ERROR: DBServer_Stats failed to connect to the database!");
	}
	
	private void initializeCheck() throws SQLException, IOException {
		
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
			dbExists = false;
			return;
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
			dbExists = false;
			return;
		}

		if(password.equals(DBServerMain.defaultPassword) || database.equals(DBServerMain.defaultDatabase)
				|| username.equals(DBServerMain.defaultUsername)){
			System.out.println("ERROR: Default username, password, and/or database detected in DBSettings.xml!");
			System.out.println("  Please change the settings in the DBSettings.xml file.");
			dbExists = false;
			return;
		}
		
		Connection con = getConnection();
		
		if(con != null && con.isValid(10))
		{
			dbExists = true;
			System.out.println("NOTE: Connection to mysql server successful!");
			DBServerMain.instance().dataManager.initialize(con);
			con.close();
		}
	}
	
	/**
	 * Attempts to get a connection to the database.
	 * @return The connection if found, null otherwise.
	 */
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
	 * Execute a query of any type.
	 * @param query The SQL query that is executed.
	 * @param connection The connection to use, if not null.
	 */
	public void anyQuery(String query, Connection connection){
		if(!dbExists)
		{
			System.out.println("WARNING: Initial connection attempt failed, therefore ignoring query!");
			return;
		}
		boolean preserveConnection = false;
		PreparedStatement statement = null;
		Connection con = null;
		if(connection == null)
			con = getConnection();
		else
		{
			preserveConnection = true;
			con = connection;
		}
		try {
			statement = con.prepareStatement(query);
			statement.execute();
		} catch (SQLException e) {
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
	 * Updates the SQL Database with the query that is an
	 * insert/update/delete.
	 * If a connection is provided (is not null), then that connection
	 * will be used and will be preserved.
	 * @param query The SQL query that is executed.
	 * @param connection The provided connection, null if not provided.
	 */
	public void updateQuery(String query, Connection connection){
		if(!dbExists)
		{
			System.out.println("WARNING: Initial connection attempt failed, therefore ignoring query!");
			return;
		}
		
		boolean preserveConnection = false;
		PreparedStatement statement = null;
		Connection con = null;
		if(connection == null)
			con = getConnection();
		else
		{
			preserveConnection = true;
			con = connection;
		}
		try {
			statement = con.prepareStatement(query);
			statement.executeUpdate();
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
		if(!dbExists)
		{
			System.out.println("WARNING: Initial connection attempt failed, therefore ignoring query!");
			return null;
		}
		
		boolean preserveConnection = false;
		PreparedStatement statement = null;
		ResultSet results = null;
		Connection con = null;
		if(connection == null)
			con = getConnection();
		else
		{
			preserveConnection = true;
			con = connection;
		}
		String result = null;
		try {
			statement = con.prepareStatement(query);
			results = statement.executeQuery();
			if(results.next())
				result = results.getString(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}

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
	
	/**
	 * Returns a collection of results as the first column from the query.
	 * If a connection is provided (is not null), then that connection is used
	 * and will be preserved.
	 * @param query The SQL Query that is executed.
	 * @param connection The provided connection, null if not provided.
	 * @return A Collection of Strings that is the result in rows form.
	 */
	public Collection<String> standardQueryColumn(String query, Connection connection) {
		if(!dbExists)
		{
			System.out.println("WARNING: Initial connection attempt failed, therefore ignoring query!");
			return null;
		}
		
		boolean preserveConnection = false;
		PreparedStatement statement = null;
		ResultSet results = null;
		Connection con = null;
		if(connection == null)
			con = getConnection();
		else
		{
			preserveConnection = true;
			con = connection;
		}
		Collection<String> resultCol = new LinkedList<String>();

		try {
			statement = con.prepareStatement(query);
			results = statement.executeQuery();
			while(results.next())
			{
				resultCol.add(results.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

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
		
		return resultCol;
	}
	
	/**
	 * Returns a collection of results as the first row from the query.
	 * If a connection is provided (is not null), then that connection is used
	 * and will be preserved.
	 * @param query The SQL Query that is executed.
	 * @param connection The provided connection, null if not provided.
	 * @param rowSize The size of the row resulting from the query.
	 * @return A Collection of Strings that is the result in rows form.
	 */
	public Collection<String> standardQueryRow(String query, Connection connection, int rowSize) {
		if(!dbExists)
		{
			System.out.println("WARNING: Initial connection attempt failed, therefore ignoring query!");
			return null;
		}
		
		boolean preserveConnection = false;
		PreparedStatement statement = null;
		ResultSet results = null;
		Connection con = null;
		if(connection == null)
			con = getConnection();
		else
		{
			preserveConnection = true;
			con = connection;
		}
		Collection<String> resultCol = new LinkedList<String>();

		try {
			statement = con.prepareStatement(query);
			results = statement.executeQuery();
			results.next();
			int i = 0;
			while(i < rowSize) {
				resultCol.add(results.getString(++i));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

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
		
		return resultCol;
	}
	
	/**
	 * Checks if a query causes an SQLException.
	 * @param query A String object containing the SQL query to check.
	 * @param connection The SQL connection to use if not null.
	 * @return True if the query caused an SQLException.
	 */
	public boolean querySQLExceptionCheck(String query, Connection connection){
		if(!dbExists)
		{
			System.out.println("WARNING: Initial connection attempt failed, therefore ignoring query!");
			return false;
		}
		
		boolean preserveConnection = false;
		boolean exceptionOccurred = false;
		PreparedStatement statement = null;
		ResultSet results = null;
		Connection con = null;
		if(connection == null)
			con = getConnection();
		else
		{
			preserveConnection = true;
			con = connection;
		}
		
		try {
			statement = con.prepareStatement(query);
			try {
				results = statement.executeQuery();
			} catch(SQLException e) {
				exceptionOccurred = true;
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
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
		
		return exceptionOccurred;
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
