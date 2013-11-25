package edu.jhu.ugrad.sseo6;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

@NetworkMod(clientSideRequired=false, serverSideRequired=true, channels = {})

@Mod(modid="mod_sseo6_DBServerStats", name="DB Server Stats", version="0.0.1")
public class DBServerMain {
	
	@Instance("mod_sseo6_DBServerStats")
	private static DBServerMain INSTANCE = new DBServerMain();
	
	protected DataManager dataManager = new DataManager();
	protected SQLManager sqlManager = new SQLManager();
	
	protected static final String modDir = "./DBServerStats";
	protected static final String xml = modDir + "/DBSettings.xml";
	
	protected static final String rootElement = "Settings";
	protected static final String infoElement = "DBInfo";
	
	protected static final String defaultHostname = "127.0.0.1";
	protected static final String defaultUsername = "(the username)";
	protected static final String defaultPassword = "(the password)";
	protected static final String defaultPort = "3306";
	protected static final String defaultDatabase = "(the database)";
	
	public static DBServerMain instance(){
		return INSTANCE;
	}
	
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
		
		sqlManager.initialize();
		
		MinecraftForge.EVENT_BUS.register(dataManager);
		GameRegistry.registerPlayerTracker(new DBPlayerTracker());
	}
	
	@EventHandler
	public void shutdown(FMLServerStoppingEvent event){
		sqlManager.updateDB();
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
			xmlWriter.writeAttribute("username", defaultUsername);
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
