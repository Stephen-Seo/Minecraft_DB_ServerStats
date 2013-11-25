package edu.jhu.ugrad.sseo6;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkMod;

@NetworkMod(clientSideRequired=false, serverSideRequired=true, channels = {})

@Mod(modid="mod_sseo6_DBServerStats", name="DB Server Stats", version="0.0.1")
public class DBServerMain {
	private SQLManager manager = new SQLManager();
	
	protected static final String modDir = "./DBServerStats";
	protected static final String xml = modDir + "/DBSettings.xml";
	
	protected static final String rootElement = "Settings";
	protected static final String infoElement = "DBInfo";
	
	protected static final String defaultHostname = "(the hostname)";
	protected static final String defaultPassword = "(the password)";
	protected static final String defaultPort = "(the port)";
	protected static final String defaultDatabase = "(the database)";
	
	@EventHandler
	public void initialize(FMLInitializationEvent event){
		File modDirFile = new File(modDir);
		if(!modDirFile.exists())
		{
			modDirFile.mkdir();
		}
		
		boolean createSettings = false;
		
		{
			File settingsFile = new File(xml);
			if(!settingsFile.exists())
				createSettings = true;
		}
		
		if(createSettings)
			createDBSettings();
		
		manager.initialize();
	}
	
	@EventHandler
	public void shutdown(FMLServerStoppingEvent event){
		manager.updateDB();
	}
	
	private void createDBSettings(){
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(xml);
			XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(fos);
			
			xmlWriter.writeStartDocument();
			xmlWriter.writeStartElement(rootElement);
			xmlWriter.writeStartElement(infoElement);
			xmlWriter.writeAttribute("hostname", defaultHostname);
			xmlWriter.writeAttribute("password", defaultPassword);
			xmlWriter.writeAttribute("port", defaultPort);
			xmlWriter.writeAttribute("database", defaultDatabase);
			xmlWriter.writeEndElement();
			xmlWriter.writeEndElement();
			xmlWriter.writeEndDocument();
			xmlWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
		}
	}
}
