package edu.jhu.ugrad.sseo6;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

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
	
	public void updateDB(){
		if(!dbExists)
			return;
	}
}
