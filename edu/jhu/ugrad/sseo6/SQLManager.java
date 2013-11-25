package edu.jhu.ugrad.sseo6;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class SQLManager {
	private boolean dbExists;
	
	public void initialize(){
		try {
			dbExists = checkConnection();
		} catch (SQLException e) {
			dbExists = false;
			System.out.println("WARNING: DBServer_Stats failed to connect to the database!");
			e.printStackTrace();
		} catch (IOException e) {
			dbExists = false;
			System.out.println("WARNING: DBServer_Stats failed to connect to the database!");
			e.printStackTrace();
		}
	}
	
	private boolean checkConnection() throws SQLException, IOException {
		String hostname = "";
		String port = "";
		String password = "";
		String database = "";
		
		FileInputStream fis = null;
		fis = new FileInputStream(DBServerMain.xml);
		try {
			XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(fis);
			while(xmlReader.hasNext())
			{
				xmlReader.next();
				if(xmlReader.isStartElement() && xmlReader.getLocalName().equals(DBServerMain.infoElement)){
					hostname = xmlReader.getAttributeValue(null, "hostname");
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

		if(hostname.equals(DBServerMain.defaultHostname) || password.equals(DBServerMain.defaultPassword)
				|| port.equals(DBServerMain.defaultPort) || database.equals(DBServerMain.defaultDatabase)){
			System.out.println("ERROR: Default hostname, port, password, and/or database detected in DBSettings.xml!");
			return false;
		}
		/*
		Connection con = null;
		Properties connectionProps = new Properties();
		*/
		return true;
	}
	
	public void updateDB(){
		if(!dbExists)
			return;
	}
}
